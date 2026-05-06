# 商品搜索性能优化实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复 N+1 查询 + 标签内存过滤，使商品搜索抗住 1000 并发

**Architecture:** 批量 SQL 替代循环查询，SQL 子查询替代内存过滤，保留 Redis 缓存

**Tech Stack:** Spring Boot + MyBatis-Plus + Redis

---

## 文件改动清单

| 文件 | 改动内容 |
|------|---------|
| `ThingServiceImpl.java` | 重写 `fillThingTags()`、优化 `getThingList()` 标签筛选逻辑 |

---

## 实现步骤

### Task 1: 重写 `fillThingTags()` 为批量查询

**Files:**
- Modify: `java_shop-master/java_shop-master/server/src/main/java/com/gk/study/service/impl/ThingServiceImpl.java:179-189`

- [ ] **Step 1: 读取当前文件确认行号**

确认 `fillThingTags` 方法在文件中的具体位置。

- [ ] **Step 2: 替换为批量查询实现**

将原来的：
```java
private void fillThingTags(List<Thing> things) {
    if (things == null) return;
    for (Thing thing : things) {
        QueryWrapper<ThingTag> thingTagQueryWrapper = new QueryWrapper<>();
        thingTagQueryWrapper.lambda().eq(ThingTag::getThingId, thing.getId());
        List<ThingTag> thingTags = thingTagMapper.selectList(thingTagQueryWrapper);
        List<Long> tags = thingTags.stream().map(ThingTag::getTagId).collect(Collectors.toList());
        thing.setTags(tags);
    }
}
```

替换为：
```java
private void fillThingTags(List<Thing> things) {
    if (things == null || things.isEmpty()) return;
    List<Long> thingIds = things.stream().map(Thing::getId).collect(Collectors.toList());

    // 一次 IN 查询拿所有关联
    QueryWrapper<ThingTag> q = new QueryWrapper<>();
    q.in("thing_id", thingIds);
    List<ThingTag> allLinks = thingTagMapper.selectList(q);

    // 按 thing_id 分组
    Map<Long, List<Long>> tagMap = allLinks.stream()
        .collect(Collectors.groupingBy(ThingTag::getThingId,
            Collectors.mapping(ThingTag::getTagId, Collectors.toList())));

    things.forEach(t -> t.setTags(tagMap.getOrDefault(t.getId(), List.of())));
}
```

注意：需要 `java.util.Map` import。

- [ ] **Step 3: 提交代码**

```bash
git add java_shop-master/java_shop-master/server/src/main/java/com/gk/study/service/impl/ThingServiceImpl.java
git commit -m "fix(thing): fillThingTags 改成批量查询，消灭 N+1"
```

---

### Task 2: 标签筛选从内存移到 SQL 层

**Files:**
- Modify: `java_shop-master/java_shop-master/server/src/main/java/com/gk/study/service/impl/ThingServiceImpl.java:67-81`

- [ ] **Step 1: 读取当前 `getThingList()` 方法确认 tag 筛选逻辑**

当前逻辑是查询出所有商品后再在 Java 里遍历过滤。改法：先用子查询拿到匹配 tag 的 thing_id，再用 IN 查询。

原逻辑（约 67-81 行）：
```java
// tag筛选
if (StringUtils.isNotBlank(tag)) {
    List<Thing> tThings = new ArrayList<>();
    QueryWrapper<ThingTag> thingTagQueryWrapper = new QueryWrapper<>();
    thingTagQueryWrapper.eq("tag_id", tag);
    List<ThingTag> thingTagList = thingTagMapper.selectList(thingTagQueryWrapper);
    for (Thing thing : things) {
        for (ThingTag thingTag : thingTagList) {
            if (thing.getId().equals(thingTag.getThingId())) {
                tThings.add(thing);
            }
        }
    }
    things.clear();
    things.addAll(tThings);
}
```

替换为：把 tag 筛选提前到 SQL 查询阶段，用子查询实现。在 `queryWrapper.like(...)` 之后、`mapper.selectList(queryWrapper)` 之前加入：

```java
// tag筛选 - 改为 SQL 子查询
if (StringUtils.isNotBlank(tag)) {
    // 先查匹配 tag 的 thing_id
    QueryWrapper<ThingTag> tagQ = new QueryWrapper<>();
    tagQ.eq("tag_id", tag);
    List<Long> matchedThingIds = thingTagMapper.selectList(tagQ).stream()
        .map(ThingTag::getThingId)
        .collect(Collectors.toList());

    if (!matchedThingIds.isEmpty()) {
        queryWrapper.in("id", matchedThingIds);
    } else {
        // 没有匹配的 tag 返回空列表
        queryWrapper.eq("id", -1L);
    }
}
```

- [ ] **Step 2: 提交代码**

```bash
git add java_shop-master/java_shop-master/server/src/main/java/com/gk/study/service/impl/ThingServiceImpl.java
git commit -m "fix(thing): 标签筛选从内存移到 SQL 子查询"
```

---

### Task 3: 验证优化效果

- [ ] **Step 1: 重启后端服务**

在 IDEA 中重启 Spring Boot 服务，确认启动无报错。

- [ ] **Step 2: 测试缓存 miss 场景**

第一次请求（无缓存）观察日志 SQL 数量，预期从 21+ 次降到 2-3 次：
```bash
curl "http://localhost:9100/thing/list?keyword=衣服&sort=hot"
```

- [ ] **Step 3: 测试缓存命中**

重复请求，观察响应时间（预期 < 50ms）。

- [ ] **Step 4: Jmeter 压测**

用 1000 并发压测，对比优化前后 QPS。

---

## 验证清单

- [ ] `fillThingTags` 批量查询生效（看日志 SQL 数量）
- [ ] 标签筛选在 SQL 层完成（无内存遍历）
- [ ] Redis 缓存正常工作（重复请求走缓存）
- [ ] 1000 并发压测通过

---

**Plan saved to:** `docs/superpowers/plans/2026-05-06-thing-search-optimization-plan.md`