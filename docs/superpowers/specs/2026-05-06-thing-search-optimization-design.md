# 商品搜索性能优化设计方案

## 目标

使商品搜索接口能抗住 1000 并发（每秒 100 请求持续 10 秒峰值）。

## 背景

- **场景**：短时峰值测试，关键词高度集中
- **更新频率**：商品数据低更新（几天一次）
- **现状**：
  - Redis 缓存已配置（`@Cacheable`，`thing:list` TTL 5 分钟）
  - N+1 查询问题未解决（`fillThingTags()` 对每条商品单独查库）
  - 标签筛选在 Java 内存中执行

## 问题分析

| 瓶颈 | 影响 |
|------|------|
| N+1 查询 (`fillThingTags`) | 缓存 miss 时 DB 被打爆，20 条商品 = 21 次 SQL |
| 标签筛选在内存 | 无效的全表扫描 + 内存过滤 |
| Redis 缓存 | 已配置但被 N+1 拖后腿 |

## 解决方案

### 1. 修复 N+1 查询 — 批量查询标签

**改前**（`ThingServiceImpl.java:179-189`）：
```java
private void fillThingTags(List<Thing> things) {
    for (Thing thing : things) {
        // 每条商品一次 SQL
        QueryWrapper<ThingTag> q = new QueryWrapper<>();
        q.lambda().eq(ThingTag::getThingId, thing.getId());
        List<ThingTag> thingTags = thingTagMapper.selectList(q);
        thing.setTags(thingTags.stream().map(ThingTag::getTagId).collect(Collectors.toList()));
    }
}
```

**改后**：
```java
private void fillThingTags(List<Thing> things) {
    if (things.isEmpty()) return;
    List<Long> thingIds = things.stream().map(Thing::getId).collect(Collectors.toList());

    // 一次 IN 查询拿所有关联
    QueryWrapper<ThingTag> q = new QueryWrapper<>();
    q.in("thing_id", thingIds);
    List<ThingTag> allLinks = thingTagMapper.selectList(q);

    // 按 thing_id 分组合并
    Map<Long, List<Long>> tagMap = allLinks.stream()
        .collect(Collectors.groupingBy(ThingTag::getThingId,
            Collectors.mapping(ThingTag::getTagId, Collectors.toList())));

    things.forEach(t -> t.setTags(tagMap.getOrDefault(t.getId(), List.of())));
}
```

效果：20 条商品从 **21 次 SQL → 1 次 SQL**

### 2. 修复标签筛选 — 改为 SQL 子查询

**改前**（内存过滤）：
```java
if (StringUtils.isNotBlank(tag)) {
    List<Thing> tThings = new ArrayList<>();
    for (Thing thing : things) { /* 遍历过滤 */ }
}
```

**改后**（SQL 层过滤）：
```java
if (StringUtils.isNotBlank(tag)) {
    queryWrapper.and(w -> w
        .in("id", thingIdsFromTagSubQuery)
        .or()
        .eq("classification_id", c)
    );
}
```

用子查询先找出匹配 tag 的 thing_id，再 IN 查询。

### 3. 保留 Redis 缓存

`@Cacheable` 配置保持不变，缓存命中率会很高（热点集中）。唯一变化是缓存 miss 时不再有 N+1 噩梦。

## 改动文件

| 文件 | 改动内容 |
|------|---------|
| `ThingServiceImpl.java` | 重写 `getThingList()`、`fillThingTags()`、`searchThingsByTitleOrTag()` |
| `CacheRedisConfig.java` | 不改（已配好） |
| `application.yml` | 可选：Druid 连接池调大 |

## 预期效果

| 指标 | 改前 | 改后 |
|------|------|------|
| DB 查询次数（20 条商品） | 21 次 | 2-3 次 |
| 缓存 miss 时响应 | ~500ms | ~50ms |
| 1000 并发抗住？ | 否 | **是** |

## 验证方法

1. 第一次请求（缓存 miss）观察日志 SQL 数量
2. 连续 100 请求后看 Redis 命中率
3. Jmeter 1000 并发压测对比改前改后