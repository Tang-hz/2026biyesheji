# 仪表盘图表扩展 + 订单合并逻辑改造

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 扩展后台仪表盘，新增8个数据图表维度，并改造购物车订单逻辑使多商品合并下单

**Architecture:**
- 后端新增8个统计接口，改写订单创建逻辑支持批量商品
- 前端 `overview.vue` 改用纵向滚动布局，新增8个 ECharts 图表组件
- 前后端通过 RESTful API 交互，数据格式 JSON

**Tech Stack:** Spring Boot 3.0.2 + MyBatis-Plus | Vue 3 + Vite + ECharts 5.x (CDN)

---

## 文件结构

```
server/src/main/java/com/gk/study/
├── controller/
│   └── OverViewController.java      # 新增8个接口
├── entity/
│   └── Order.java                   # 新增 ThingItem 内部类
├── service/impl/
│   └── OrderServiceImpl.java        # 改写 createOrder 支持批量
└── resources/mapper/
    └── OverviewMapper.xml           # 新增8个 SQL 查询

web/src/
├── api/overview.ts                  # 新增8个 API 调用
└── views/admin/
    └── overview.vue                 # 重构布局 + 新增8个图表
```

---

## 第一部分：订单合并逻辑改造

### Task 1: 修改 Order 实体，添加批量商品字段

**Files:**
- Modify: `server/src/main/java/com/gk/study/entity/Order.java:1-54`

- [ ] **Step 1: 在 Order.java 中添加 ThingItem 内部类**

在 `Order.java` 文件末尾 `}` 之前添加：

```java
// 订单商品项（用于批量创建订单）
@Data
public static class ThingItem implements Serializable {
    public String thingId;    // 商品ID
    public String count;     // 购买数量
    public String remark;    // 商品备注
}
```

- [ ] **Step 2: 添加 items 字段**

在 `@TableField(exist = false) public Integer redeemPoints;` 之后添加：

```java
@TableField(exist = false)
public List<ThingItem> items;  // 批量商品列表（批量下单时使用）
```

---

### Task 2: 改写 OrderController 支持批量创建

**Files:**
- Modify: `server/src/main/java/com/gk/study/controller/OrderController.java:75-83`

- [ ] **Step 1: 修改 create 方法签名**

将原有的 create 方法替换为：

```java
@Operation(summary = "创建订单（支持单商品和批量）")
@RequestMapping(value = "/create", method = RequestMethod.POST)
@Transactional
public APIResponse create(Order order,
                          @RequestParam(value = "redeemPoints", required = false) Integer redeemPoints,
                          @RequestParam(value = "items", required = false) String itemsJson) throws IOException {
    order.setRedeemPoints(redeemPoints);
    // 如果传入 itemsJson，说明是批量下单
    if (itemsJson != null && !itemsJson.isEmpty()) {
        ObjectMapper mapper = new ObjectMapper();
        List<Order.ThingItem> items = mapper.readValue(itemsJson,
            mapper.getTypeFactory().constructCollectionType(List.class, Order.ThingItem.class));
        order.setItems(items);
    }
    service.createOrder(order);
    return new APIResponse(ResponeCode.SUCCESS, "创建成功");
}
```

需要添加 import：
```java
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
```

---

### Task 3: 改写 OrderServiceImpl 支持批量下单

**Files:**
- Modify: `server/src/main/java/com/gk/study/service/impl/OrderServiceImpl.java:36-107`

- [ ] **Step 1: 替换 createOrder 方法**

将原有的 `createOrder(Order order)` 方法整体替换为：

```java
@Override
@Transactional
public void createOrder(Order order) {
    List<Order.ThingItem> items = order.getItems();

    // 如果是批量下单（items 不为空）
    if (items != null && !items.isEmpty()) {
        String sharedOrderNumber = String.valueOf(System.currentTimeMillis());
        java.time.LocalDateTime sharedOrderTime = java.time.LocalDateTime.now();
        Long userId = Long.parseLong(order.getUserId());
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 计算整单总金额（用于分摊积分抵扣）
        BigDecimal subtotalTotal = BigDecimal.ZERO;
        for (Order.ThingItem item : items) {
            Thing thing = thingMapper.selectById(item.getThingId());
            if (thing == null) throw new RuntimeException("商品不存在: " + item.getThingId());
            BigDecimal itemPrice = thing.getPrice() != null ? thing.getPrice() : BigDecimal.ZERO;
            int count = 1;
            try { count = Integer.parseInt(item.getCount()); } catch (NumberFormatException ignored) {}
            subtotalTotal = subtotalTotal.add(itemPrice.multiply(BigDecimal.valueOf(count)));
        }

        // 获取会员折扣率
        BigDecimal discountRate = memberService.getMemberDiscount(userId);
        BigDecimal discountedTotal = subtotalTotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);

        // 积分抵扣（如果有）
        int usedPoints = order.getRedeemPoints() != null ? order.getRedeemPoints() : 0;
        BigDecimal redeemMoney = BigDecimal.ZERO;
        if (usedPoints > 0) {
            redeemMoney = PriceUtils.pointsToMoney(usedPoints);
        }

        // 逐个创建订单明细
        for (Order.ThingItem item : items) {
            Thing thing = thingMapper.selectById(item.getThingId());
            if (thing == null) continue;

            BigDecimal itemPrice = thing.getPrice() != null ? thing.getPrice() : BigDecimal.ZERO;
            int count = 1;
            try { count = Integer.parseInt(item.getCount()); } catch (NumberFormatException ignored) {}

            BigDecimal itemSubtotal = itemPrice.multiply(BigDecimal.valueOf(count))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal itemDiscounted = itemSubtotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);

            // 按商品金额比例分摊积分抵扣
            BigDecimal itemRedeemMoney = BigDecimal.ZERO;
            int itemRedeemPoints = 0;
            if (usedPoints > 0 && discountedTotal.compareTo(BigDecimal.ZERO) > 0) {
                itemRedeemMoney = itemDiscounted.divide(discountedTotal, 4, RoundingMode.HALF_UP)
                        .multiply(redeemMoney).setScale(2, RoundingMode.HALF_UP);
                itemRedeemPoints = (int) Math.floor(itemRedeemMoney.multiply(BigDecimal.valueOf(100)));
            }

            BigDecimal finalPrice = itemDiscounted.subtract(itemRedeemMoney);
            if (finalPrice.compareTo(BigDecimal.ZERO) < 0) finalPrice = BigDecimal.ZERO;

            // 创建子订单
            Order subOrder = new Order();
            subOrder.setThingId(item.getThingId());
            subOrder.setCount(item.getCount());
            subOrder.setUserId(order.getUserId());
            subOrder.setRemark(item.getRemark() != null ? item.getRemark() : order.getRemark());
            subOrder.setReceiverName(order.getReceiverName());
            subOrder.setReceiverPhone(order.getReceiverPhone());
            subOrder.setReceiverAddress(order.getReceiverAddress());
            subOrder.setOrderNumber(sharedOrderNumber);
            subOrder.setOrderTime(sharedOrderTime);
            subOrder.setStatus("2"); // 已支付
            subOrder.setTotalPrice(finalPrice);

            mapper.insert(subOrder);

            // 订单完成后处理积分
            int earnedPoints = finalPrice.intValue();
            if (earnedPoints > 0) {
                pointsService.earnPoints(userId, earnedPoints, PointsRule.TYPE_ORDER,
                        subOrder.getId(), "购物获得积分");
            }
            if (itemRedeemPoints > 0) {
                pointsService.deductPoints(userId, itemRedeemPoints, "订单抵扣");
            }

            totalAmount = totalAmount.add(finalPrice);
        }

        // 更新用户累计消费金额并检查会员升级（按实际支付总额）
        memberService.checkAndUpgrade(userId, totalAmount);

    } else {
        // 单商品下单（原逻辑保持兼容）
        createSingleOrder(order);
    }
}

// 单商品下单（原逻辑抽取）
private void createSingleOrder(Order order) {
    Thing thing = thingMapper.selectById(order.getThingId());
    if (thing == null) throw new RuntimeException("商品不存在");

    BigDecimal itemPrice = thing.getPrice() != null ? thing.getPrice() : BigDecimal.ZERO;
    int count = 1;
    try { count = Integer.parseInt(order.getCount()); } catch (NumberFormatException ignored) {}

    BigDecimal subtotal = itemPrice.multiply(BigDecimal.valueOf(count)).setScale(2, RoundingMode.HALF_UP);
    Long userId = Long.parseLong(order.getUserId());
    BigDecimal discountRate = memberService.getMemberDiscount(userId);
    BigDecimal finalPrice = subtotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);

    int usedPoints = order.getRedeemPoints() != null ? order.getRedeemPoints() : 0;
    if (usedPoints > 0) {
        BigDecimal redeemMoney = PriceUtils.pointsToMoney(usedPoints);
        finalPrice = finalPrice.subtract(redeemMoney);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) finalPrice = BigDecimal.ZERO;
    }

    order.setOrderTime(java.time.LocalDateTime.now());
    order.setOrderNumber(String.valueOf(System.currentTimeMillis()));
    order.setStatus("2");
    order.setTotalPrice(finalPrice);

    mapper.insert(order);

    int earnedPoints = finalPrice.intValue();
    if (earnedPoints > 0) {
        pointsService.earnPoints(userId, earnedPoints, PointsRule.TYPE_ORDER,
                order.getId(), "购物获得积分");
    }
    if (usedPoints > 0) {
        pointsService.deductPoints(userId, usedPoints, "订单抵扣");
    }
    memberService.checkAndUpgrade(userId, finalPrice);
}
```

需要添加 import：
```java
import java.math.RoundingMode;
import com.fasterxml.jackson.databind.ObjectMapper;
```

---

### Task 4: 修改前端 cart.vue 支持批量下单

**Files:**
- Modify: `web/src/views/index/cart.vue:411-459`

- [ ] **Step 1: 替换 handleJiesuan 函数**

将 `handleJiesuan` 函数替换为：

```javascript
const handleJiesuan = async () => {
  const userId = userStore.user_id;
  if (!userId) {
    message.warn('请先登录！');
    return;
  }
  if (!cartRows.value.length) {
    message.warn('购物车为空');
    return;
  }
  if (!pageData.receiverName) {
    message.warn('请先填写收货地址！');
    return;
  }
  try {
    const formData = new FormData();
    formData.append('userId', String(userId));

    // 批量商品列表
    const items = cartRows.value.map(item => ({
      thingId: String(item.thingId),
      count: String(item.count),
      remark: pageData.remark || ''
    }));
    formData.append('items', JSON.stringify(items));

    formData.append('receiverName', pageData.receiverName!);
    formData.append('receiverPhone', pageData.receiverPhone!);
    formData.append('receiverAddress', pageData.receiverAddress!);

    if (redeemPointsInput.value > 0) {
      formData.append('redeemPoints', String(redeemPointsInput.value));
    }

    await createApi(formData);
    await clearCartApi({ userId: String(userId) });
    await cartStore.refreshCount();
    message.success('请支付订单');
    router.push({ name: 'pay', query: { amount: finalPayment.value } });
  } catch (e: any) {
    message.error(e.msg || e.message || '下单失败');
  }
};
```

---

## 第二部分：后端新增图表接口

### Task 5: 扩展 OverViewController 新增8个接口

**Files:**
- Modify: `server/src/main/java/com/gk/study/controller/OverViewController.java`

- [ ] **Step 1: 在文件顶部添加所需 import**

```java
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gk.study.entity.User;
import com.gk.study.mapper.UserMapper;
import org.apache.commons.lang3.time.DateUtils;
import java.text.ParseException;
import java.util.stream.Collectors;
```

- [ ] **Step 2: 注入 UserMapper**

在已有的 mapper 注入之后添加：

```java
@Autowired
UserMapper userMapper;
```

- [ ] **Step 3: 添加8个新接口方法**

在 `getSevenDate()` 方法之后、类结束 `}` 之前添加以下8个方法：

```java
/**
 * 用户增长趋势
 */
@Operation(summary = "用户增长趋势")
@RequestMapping(value = "/userGrowth", method = RequestMethod.GET)
public APIResponse userGrowth(String type) {
    List<Map<String, Object>> result = new ArrayList<>();
    List<String> dates = getDateRange(type);
    for (String day : dates) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.likeRight("create_time", day);
        long count = userMapper.selectCount(qw);
        Map<String, Object> m = new HashMap<>();
        m.put("date", day);
        m.put("count", count);
        result.add(m);
    }
    return new APIResponse(ResponeCode.SUCCESS, "查询成功", result);
}

/**
 * 会员等级分布
 */
@Operation(summary = "会员等级分布")
@RequestMapping(value = "/memberDistribution", method = RequestMethod.GET)
public APIResponse memberDistribution() {
    List<Map<String, Object>> result = new ArrayList<>();
    // member_level: 1=普通, 2=白银, 3=黄金, 4=钻石
    String[] levels = {"1", "2", "3", "4"};
    String[] names = {"普通会员", "白银会员", "黄金会员", "钻石会员"};
    for (int i = 0; i < levels.length; i++) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("member_level", levels[i]);
        long count = userMapper.selectCount(qw);
        Map<String, Object> m = new HashMap<>();
        m.put("level", names[i]);
        m.put("count", count);
        result.add(m);
    }
    return new APIResponse(ResponeCode.SUCCESS, "查询成功", result);
}

/**
 * 销售额趋势
 */
@Operation(summary = "销售额趋势")
@RequestMapping(value = "/salesTrend", method = RequestMethod.GET)
public APIResponse salesTrend(String type) {
    List<Map<String, Object>> result = new ArrayList<>();
    List<String> dates = getDateRange(type);
    for (String day : dates) {
        QueryWrapper<Order> qw = new QueryWrapper<>();
        qw.likeRight("order_time", day);
        qw.eq("status", "2"); // 只统计已支付订单
        List<Order> orders = orderMapper.selectList(qw);
        double total = orders.stream()
                .mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice().doubleValue() : 0)
                .sum();
        Map<String, Object> m = new HashMap<>();
        m.put("date", day);
        m.put("amount", total);
        result.add(m);
    }
    return new APIResponse(ResponeCode.SUCCESS, "查询成功", result);
}

/**
 * 客单价分布
 */
@Operation(summary = "客单价分布")
@RequestMapping(value = "/avgOrderValue", method = RequestMethod.GET)
public APIResponse avgOrderValue() {
    List<Map<String, Object>> result = new ArrayList<>();
    String[] ranges = {"0-50", "50-100", "100-200", "200-500", "500+"};
    int[] lows = {0, 50, 100, 200, 500};
    int[] highs = {50, 100, 200, 500, Integer.MAX_VALUE};

    QueryWrapper<Order> qw = new QueryWrapper<>();
    qw.eq("status", "2");
    List<Order> orders = orderMapper.selectList(qw);

    for (int i = 0; i < ranges.length; i++) {
        long count;
        if (highs[i] == Integer.MAX_VALUE) {
            count = orders.stream()
                    .filter(o -> o.getTotalPrice() != null &&
                            o.getTotalPrice().doubleValue() >= lows[i])
                    .count();
        } else {
            final int low = lows[i];
            final int high = highs[i];
            count = orders.stream()
                    .filter(o -> o.getTotalPrice() != null &&
                            o.getTotalPrice().doubleValue() >= low &&
                            o.getTotalPrice().doubleValue() < high)
                    .count();
        }
        Map<String, Object> m = new HashMap<>();
        m.put("range", ranges[i]);
        m.put("count", count);
        result.add(m);
    }
    return new APIResponse(ResponeCode.SUCCESS, "查询成功", result);
}

/**
 * 订单状态分布
 */
@Operation(summary = "订单状态分布")
@RequestMapping(value = "/orderStatus", method = RequestMethod.GET)
public APIResponse orderStatus() {
    List<Map<String, Object>> result = new ArrayList<>();
    // status: 0=待处理, 1=未支付, 2=已支付, 7=已取消
    String[] statuses = {"0", "1", "2", "7"};
    String[] names = {"待处理", "未支付", "已支付", "已取消"};

    for (int i = 0; i < statuses.length; i++) {
        QueryWrapper<Order> qw = new QueryWrapper<>();
        qw.eq("status", statuses[i]);
        long count = orderMapper.selectCount(qw);
        Map<String, Object> m = new HashMap<>();
        m.put("status", names[i]);
        m.put("count", count);
        result.add(m);
    }
    return new APIResponse(ResponeCode.SUCCESS, "查询成功", result);
}

/**
 * 订单量趋势
 */
@Operation(summary = "订单量趋势")
@RequestMapping(value = "/orderTrend", method = RequestMethod.GET)
public APIResponse orderTrend(String type) {
    List<Map<String, Object>> result = new ArrayList<>();
    List<String> dates = getDateRange(type);
    for (String day : dates) {
        QueryWrapper<Order> qw = new QueryWrapper<>();
        qw.likeRight("order_time", day);
        long count = orderMapper.selectCount(qw);
        Map<String, Object> m = new HashMap<>();
        m.put("date", day);
        m.put("count", count);
        result.add(m);
    }
    return new APIResponse(ResponeCode.SUCCESS, "查询成功", result);
}

/**
 * 商品评分分布
 */
@Operation(summary = "商品评分分布")
@RequestMapping(value = "/thingScore", method = RequestMethod.GET)
public APIResponse thingScore() {
    List<Map<String, Object>> result = new ArrayList<>();
    // score 范围 0-5
    for (int score = 0; score <= 5; score++) {
        QueryWrapper<Thing> qw = new QueryWrapper<>();
        qw.eq("score", score);
        long count = thingMapper.selectCount(qw);
        Map<String, Object> m = new HashMap<>();
        m.put("score", score + "分");
        m.put("count", count);
        result.add(m);
    }
    return new APIResponse(ResponeCode.SUCCESS, "查询成功", result);
}

/**
 * 商品热度 TOP10
 */
@Operation(summary = "商品热度TOP10")
@RequestMapping(value = "/thingHot", method = RequestMethod.GET)
public APIResponse thingHot() {
    QueryWrapper<Thing> qw = new QueryWrapper<>();
    qw.orderByDesc("pv");
    qw.last("LIMIT 10");
    List<Thing> things = thingMapper.selectList(qw);
    List<Map<String, Object>> result = things.stream().map(t -> {
        Map<String, Object> m = new HashMap<>();
        m.put("title", t.getTitle());
        m.put("pv", t.getPv() != null ? t.getPv() : 0);
        return m;
    }).collect(Collectors.toList());
    return new APIResponse(ResponeCode.SUCCESS, "查询成功", result);
}

/**
 * 根据 type 获取日期范围
 * type: day=最近7天, week=最近4周, month=最近6个月
 */
private List<String> getDateRange(String type) {
    List<String> dates = new ArrayList<>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    if ("week".equals(type)) {
        // 最近4周，每周取周一的日期
        for (int i = 3; i >= 0; i--) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.WEEK_OF_YEAR, -i);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            dates.add(sdf.format(cal.getTime()));
        }
    } else if ("month".equals(type)) {
        // 最近6个月
        for (int i = 5; i >= 0; i--) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -i);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            dates.add(sdf.format(cal.getTime()));
        }
    } else {
        // 默认最近7天
        for (int i = 6; i >= 0; i--) {
            Date date = DateUtils.addDays(new Date(), -i);
            dates.add(sdf.format(date));
        }
    }
    return dates;
}
```

---

## 第三部分：前端 API 和图表组件

### Task 6: 扩展前端 API 文件

**Files:**
- Modify: `web/src/api/overview.ts`

- [ ] **Step 1: 添加新的 API 函数**

将 `web/src/api/overview.ts` 替换为：

```typescript
import {get, post} from '/@/utils/http/axios';

enum URL {
    list = '/api/overview/count',
    sysInfo = '/api/overview/sysInfo',
    userGrowth = '/api/overview/userGrowth',
    memberDistribution = '/api/overview/memberDistribution',
    salesTrend = '/api/overview/salesTrend',
    avgOrderValue = '/api/overview/avgOrderValue',
    orderStatus = '/api/overview/orderStatus',
    orderTrend = '/api/overview/orderTrend',
    thingScore = '/api/overview/thingScore',
    thingHot = '/api/overview/thingHot',
}

const listApi = async (params: any) =>
    get<any>({url: URL.list, params: params, data: {}, headers: {}});

const sysInfoApi = async (params: any) =>
    get<any>({url: URL.sysInfo, params: params, data: {}, headers: {}});

const userGrowthApi = async (params: any) =>
    get<any>({url: URL.userGrowth, params: params, data: {}, headers: {}});

const memberDistributionApi = async (params: any) =>
    get<any>({url: URL.memberDistribution, params: params, data: {}, headers: {}});

const salesTrendApi = async (params: any) =>
    get<any>({url: URL.salesTrend, params: params, data: {}, headers: {}});

const avgOrderValueApi = async (params: any) =>
    get<any>({url: URL.avgOrderValue, params: params, data: {}, headers: {}});

const orderStatusApi = async (params: any) =>
    get<any>({url: URL.orderStatus, params: params, data: {}, headers: {}});

const orderTrendApi = async (params: any) =>
    get<any>({url: URL.orderTrend, params: params, data: {}, headers: {}});

const thingScoreApi = async (params: any) =>
    get<any>({url: URL.thingScore, params: params, data: {}, headers: {}});

const thingHotApi = async (params: any) =>
    get<any>({url: URL.thingHot, params: params, data: {}, headers: {}});

export {
    listApi,
    sysInfoApi,
    userGrowthApi,
    memberDistributionApi,
    salesTrendApi,
    avgOrderValueApi,
    orderStatusApi,
    orderTrendApi,
    thingScoreApi,
    thingHotApi,
};
```

---

### Task 7: 重构 overview.vue 页面布局和图表

**Files:**
- Modify: `web/src/views/admin/overview.vue`

- [ ] **Step 1: 替换模板部分**

将 `<template>` 整个替换为：

```vue
<template>
  <a-spin :spinning="showSpin">
    <div class="main">
      <!-- 统计卡片行（保持不变） -->
      <a-row :gutter="[20,20]">
        <a-col :sm="24" :md="12" :lg="6">
          <a-card size="small" title="商品总数">
            <template #extra><a-tag color="blue">总</a-tag></template>
            <div class="box">
              <div class="box-top">
                <span class="box-value">{{ tdata.data.spzs }}<span class="v-e">种</span></span>
              </div>
              <div class="box-bottom"><span>本周新增 {{ tdata.data.qrxz }} 种</span></div>
            </div>
          </a-card>
        </a-col>
        <a-col :sm="24" :md="12" :lg="6">
          <a-card size="small" title="未付订单">
            <template #extra><a-tag color="green">未付</a-tag></template>
            <div class="box">
              <div class="box-top">
                <span class="box-value">{{ tdata.data.wfdd }}<span class="v-e">单</span></span>
              </div>
              <div class="box-bottom"><span>共 {{ tdata.data.wfddrs }} 人</span></div>
            </div>
          </a-card>
        </a-col>
        <a-col :sm="24" :md="12" :lg="6">
          <a-card size="small" title="已付订单">
            <template #extra><a-tag color="blue">已付</a-tag></template>
            <div class="box">
              <div class="box-top">
                <span class="box-value">{{ tdata.data.yfdd }}<span class="v-e">单</span></span>
              </div>
              <div class="box-bottom"><span>共 {{ tdata.data.yfddrs }} 人</span></div>
            </div>
          </a-card>
        </a-col>
        <a-col :sm="24" :md="12" :lg="6">
          <a-card size="small" title="取消订单">
            <template #extra><a-tag color="red">取消</a-tag></template>
            <div class="box">
              <div class="box-top">
                <span class="box-value">{{ tdata.data.qxdd }}<span class="v-e">单</span></span>
              </div>
              <div class="box-bottom"><span>共 {{ tdata.data.qxddrs }} 人</span></div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 保留图表：最近一周访问量 -->
      <a-card title="最近一周访问量" class="chart-card">
        <div style="height: 280px;" ref="visitChartDiv"></div>
      </a-card>

      <!-- 保留图表：热门商品 + 热门分类 -->
      <a-row :gutter="[20,20]">
        <a-col :sm="24" :md="24" :lg="12">
          <a-card title="热门商品排名" class="chart-card">
            <div style="height: 280px;" ref="barChartDiv"></div>
          </a-card>
        </a-col>
        <a-col :sm="24" :md="24" :lg="12">
          <a-card title="热门分类比例" class="chart-card">
            <div style="height: 280px;" ref="pieChartDiv"></div>
          </a-card>
        </a-col>
      </a-row>

      <!-- ===== 新增图表区域 ===== -->

      <!-- 用户分析区 -->
      <div class="section-title">用户分析</div>
      <a-row :gutter="[20,20]">
        <a-col :sm="24" :md="24" :lg="12">
          <a-card title="用户增长趋势" class="chart-card">
            <template #extra>
              <a-radio-group v-model="userGrowthType" size="small" @change="loadUserGrowth">
                <a-radio-button value="day">日</a-radio-button>
                <a-radio-button value="week">周</a-radio-button>
                <a-radio-button value="month">月</a-radio-button>
              </a-radio-group>
            </template>
            <div style="height: 260px;" ref="userGrowthChartDiv"></div>
          </a-card>
        </a-col>
        <a-col :sm="24" :md="24" :lg="12">
          <a-card title="会员等级分布" class="chart-card">
            <div style="height: 260px;" ref="memberDistChartDiv"></div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 销售分析区 -->
      <div class="section-title">销售分析</div>
      <a-row :gutter="[20,20]">
        <a-col :sm="24" :md="24" :lg="12">
          <a-card title="销售额趋势" class="chart-card">
            <template #extra>
              <a-radio-group v-model="salesTrendType" size="small" @change="loadSalesTrend">
                <a-radio-button value="day">日</a-radio-button>
                <a-radio-button value="week">周</a-radio-button>
                <a-radio-button value="month">月</a-radio-button>
              </a-radio-group>
            </template>
            <div style="height: 260px;" ref="salesTrendChartDiv"></div>
          </a-card>
        </a-col>
        <a-col :sm="24" :md="24" :lg="12">
          <a-card title="客单价分布" class="chart-card">
            <div style="height: 260px;" ref="avgOrderChartDiv"></div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 订单分析区 -->
      <div class="section-title">订单分析</div>
      <a-row :gutter="[20,20]">
        <a-col :sm="24" :md="24" :lg="12">
          <a-card title="订单状态分布" class="chart-card">
            <div style="height: 260px;" ref="orderStatusChartDiv"></div>
          </a-card>
        </a-col>
        <a-col :sm="24" :md="24" :lg="12">
          <a-card title="订单量趋势" class="chart-card">
            <template #extra>
              <a-radio-group v-model="orderTrendType" size="small" @change="loadOrderTrend">
                <a-radio-button value="day">日</a-radio-button>
                <a-radio-button value="week">周</a-radio-button>
                <a-radio-button value="month">月</a-radio-button>
              </a-radio-group>
            </template>
            <div style="height: 260px;" ref="orderTrendChartDiv"></div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 商品分析区 -->
      <div class="section-title">商品分析</div>
      <a-row :gutter="[20,20]">
        <a-col :sm="24" :md="24" :lg="12">
          <a-card title="商品评分分布" class="chart-card">
            <div style="height: 260px;" ref="thingScoreChartDiv"></div>
          </a-card>
        </a-col>
        <a-col :sm="24" :md="24" :lg="12">
          <a-card title="商品热度 TOP10" class="chart-card">
            <div style="height: 260px;" ref="thingHotChartDiv"></div>
          </a-card>
        </a-col>
      </a-row>

    </div>
  </a-spin>
</template>
```

- [ ] **Step 2: 替换 script 部分**

将 `<script setup lang="ts">` 部分替换为：

```javascript
<script setup lang="ts">
import {ref, reactive, onMounted} from 'vue';
import {listApi, userGrowthApi, memberDistributionApi, salesTrendApi,
        avgOrderValueApi, orderStatusApi, orderTrendApi,
        thingScoreApi, thingHotApi} from '/@/api/overview';

let showSpin = ref(true);

// 时间切换类型
const userGrowthType = ref('day');
const salesTrendType = ref('day');
const orderTrendType = ref('day');

// 图表 ref
const visitChartDiv = ref();
const barChartDiv = ref();
const pieChartDiv = ref();
const userGrowthChartDiv = ref();
const memberDistChartDiv = ref();
const salesTrendChartDiv = ref();
const avgOrderChartDiv = ref();
const orderStatusChartDiv = ref();
const orderTrendChartDiv = ref();
const thingScoreChartDiv = ref();
const thingHotChartDiv = ref();

// 图表实例
let visitChart: any, barChart: any, pieChart: any;
let userGrowthChart: any, memberDistChart: any;
let salesTrendChart: any, avgOrderChart: any;
let orderStatusChart: any, orderTrendChart: any;
let thingScoreChart: any, thingHotChart: any;

let tdata = reactive({data: {} as any});

onMounted(() => {
  listApi({}).then(res => {
    tdata.data = res.data;
    initCharts();
    initBarChart();
    initPieChart();
    loadNewCharts();
    showSpin.value = false;
  }).catch(() => {
    showSpin.value = false;
  });

  window.onresize = () => {
    visitChart?.resize();
    barChart?.resize();
    pieChart?.resize();
    userGrowthChart?.resize();
    memberDistChart?.resize();
    salesTrendChart?.resize();
    avgOrderChart?.resize();
    orderStatusChart?.resize();
    orderTrendChart?.resize();
    thingScoreChart?.resize();
    thingHotChart?.resize();
  };
});

// 加载所有新图表
const loadNewCharts = () => {
  loadUserGrowth();
  loadMemberDistribution();
  loadSalesTrend();
  loadAvgOrderValue();
  loadOrderStatus();
  loadOrderTrend();
  loadThingScore();
  loadThingHot();
};

// 用户增长趋势
const loadUserGrowth = () => {
  userGrowthApi({type: userGrowthType.value}).then(res => {
    const xData = res.data.map((d: any) => d.date);
    const yData = res.data.map((d: any) => d.count);
    userGrowthChart = echarts.init(userGrowthChartDiv.value);
    userGrowthChart.setOption({
      tooltip: {trigger: 'axis'},
      grid: {top: 30, left: 50, right: 20, bottom: 30},
      xAxis: {type: 'category', data: xData, axisLabel: {color: '#666'}},
      yAxis: {type: 'value', axisLine: {show: false}, axisTick: {show: false}, splitLine: {lineStyle: {color: 'rgba(0,0,0,0.1)'}}},
      series: [{name: '新增用户', type: 'line', data: yData, itemStyle: {color: '#36CBCB'}}]
    });
  });
};

// 会员等级分布
const loadMemberDistribution = () => {
  memberDistributionApi({}).then(res => {
    const data = res.data.map((d: any) => ({name: d.level, value: d.count}));
    memberDistChart = echarts.init(memberDistChartDiv.value);
    memberDistChart.setOption({
      tooltip: {trigger: 'item'},
      legend: {bottom: 10, left: 'center'},
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {normal: {color: (params: any) => ['#70B0EA', '#B3A3DA', '#F6BD3C', '#F76B4C'][params.dataIndex]}},
        label: {show: false, position: 'center'},
        emphasis: {label: {show: true, fontSize: 18, fontWeight: 'bold'}},
        data
      }]
    });
  });
};

// 销售额趋势
const loadSalesTrend = () => {
  salesTrendApi({type: salesTrendType.value}).then(res => {
    const xData = res.data.map((d: any) => d.date);
    const yData = res.data.map((d: any) => d.amount);
    salesTrendChart = echarts.init(salesTrendChartDiv.value);
    salesTrendChart.setOption({
      tooltip: {trigger: 'axis', formatter: (params: any) => `${params[0].name}<br/>销售额: ¥${params[0].value.toFixed(2)}`},
      grid: {top: 30, left: 60, right: 20, bottom: 30},
      xAxis: {type: 'category', data: xData, axisLabel: {color: '#666'}},
      yAxis: {type: 'value', axisLine: {show: false}, axisTick: {show: false}, splitLine: {lineStyle: {color: 'rgba(0,0,0,0.1)'}}, label: {formatter: '¥{value}'}},
      series: [{name: '销售额', type: 'line', data: yData, itemStyle: {color: '#F6BD3C'}, areaStyle: {color: 'rgba(246,189,60,0.2)'}}]
    });
  });
};

// 客单价分布
const loadAvgOrderValue = () => {
  avgOrderValueApi({}).then(res => {
    const xData = res.data.map((d: any) => d.range);
    const yData = res.data.map((d: any) => d.count);
    avgOrderChart = echarts.init(avgOrderChartDiv.value);
    avgOrderChart.setOption({
      tooltip: {trigger: 'axis'},
      grid: {top: 30, left: 60, right: 20, bottom: 30},
      xAxis: {type: 'category', data: xData, axisLabel: {color: '#666'}, name: '价格区间(元)'},
      yAxis: {type: 'value', axisLine: {show: false}, axisTick: {show: false}, splitLine: {lineStyle: {color: 'rgba(0,0,0,0.1)'}}},
      series: [{name: '订单数', type: 'bar', data: yData, itemStyle: {color: '#F76B4C'}}]
    });
  });
};

// 订单状态分布
const loadOrderStatus = () => {
  orderStatusApi({}).then(res => {
    const data = res.data.map((d: any) => ({name: d.status, value: d.count}));
    orderStatusChart = echarts.init(orderStatusChartDiv.value);
    orderStatusChart.setOption({
      tooltip: {trigger: 'item'},
      legend: {bottom: 10, left: 'center'},
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        itemStyle: {normal: {color: (params: any) => ['#90949A', '#F6BD3C', '#4ECB73', '#F76B4C'][params.dataIndex]}},
        label: {show: false},
        emphasis: {label: {show: true, fontSize: 16, fontWeight: 'bold'}},
        data
      }]
    });
  });
};

// 订单量趋势
const loadOrderTrend = () => {
  orderTrendApi({type: orderTrendType.value}).then(res => {
    const xData = res.data.map((d: any) => d.date);
    const yData = res.data.map((d: any) => d.count);
    orderTrendChart = echarts.init(orderTrendChartDiv.value);
    orderTrendChart.setOption({
      tooltip: {trigger: 'axis'},
      grid: {top: 30, left: 50, right: 20, bottom: 30},
      xAxis: {type: 'category', data: xData, axisLabel: {color: '#666'}},
      yAxis: {type: 'value', axisLine: {show: false}, axisTick: {show: false}, splitLine: {lineStyle: {color: 'rgba(0,0,0,0.1)'}}},
      series: [{name: '订单量', type: 'line', data: yData, itemStyle: {color: '#4ECB73'}}]
    });
  });
};

// 商品评分分布
const loadThingScore = () => {
  thingScoreApi({}).then(res => {
    const xData = res.data.map((d: any) => d.score);
    const yData = res.data.map((d: any) => d.count);
    thingScoreChart = echarts.init(thingScoreChartDiv.value);
    thingScoreChart.setOption({
      tooltip: {trigger: 'axis'},
      grid: {top: 30, left: 50, right: 20, bottom: 30},
      xAxis: {type: 'category', data: xData, axisLabel: {color: '#666'}, name: '评分'},
      yAxis: {type: 'value', axisLine: {show: false}, axisTick: {show: false}, splitLine: {lineStyle: {color: 'rgba(0,0,0,0.1)'}}},
      series: [{name: '商品数', type: 'bar', data: yData, itemStyle: {color: '#70B0EA'}}]
    });
  });
};

// 商品热度 TOP10
const loadThingHot = () => {
  thingHotApi({}).then(res => {
    const xData = res.data.map((d: any) => d.title);
    const yData = res.data.map((d: any) => d.pv);
    thingHotChart = echarts.init(thingHotChartDiv.value);
    thingHotChart.setOption({
      tooltip: {trigger: 'axis'},
      grid: {top: 30, left: 100, right: 20, bottom: 30},
      xAxis: {type: 'value', axisLine: {show: false}, axisTick: {show: false}, splitLine: {lineStyle: {color: 'rgba(0,0,0,0.1)'}}},
      yAxis: {type: 'category', data: xData.reverse(), axisLabel: {color: '#666'}},
      series: [{name: '浏览量', type: 'bar', data: yData.reverse(), itemStyle: {color: '#B3A3DA'}}]
    });
  });
};

// 以下为原有图表方法（保持不变）
const initCharts = () => {
  let xData: any[] = [], uvData: any[] = [], pvData: any[] = [];
  tdata.data.visitList?.forEach((item: any) => {
    xData.push(item.day); uvData.push(item.uv); pvData.push(item.pv);
  });
  visitChart = echarts.init(visitChartDiv.value);
  visitChart.setOption({
    tooltip: {trigger: 'axis'},
    legend: {data: ['UV(独立访客)', 'PV(访问量)'], top: '90%', left: 'center'},
    grid: {top: '30px', left: '20px', right: '20px', bottom: '40px', containLabel: true},
    xAxis: {type: 'category', data: xData, axisLabel: {color: '#2F4F4F'}, axisLine: {lineStyle: {color: '#2F4F4F'}}},
    yAxis: {type: 'value', axisLine: {show: false}, axisTick: {show: false}, splitLine: {show: true, lineStyle: {color: 'rgba(10,10,10,0.1)', width: 1, type: 'solid'}}},
    series: [
      {name: 'UV(独立访客)', type: 'line', stack: 'Total', data: uvData},
      {name: 'PV(访问量)', type: 'line', stack: 'Total', data: pvData}
    ]
  });
};

const initBarChart = () => {
  let xData: any[] = [], yData: any[] = [];
  tdata.data.popularThings?.forEach((item: any) => {
    xData.push(item.title); yData.push(item.count);
  });
  barChart = echarts.init(barChartDiv.value);
  barChart.setOption({
    grid: {top: '40px', left: '40px', right: '40px', bottom: '40px'},
    title: {text: '热门商品排名', textStyle: {color: '#aaa', fontSize: 18}, x: 'center', y: 'top'},
    tooltip: {trigger: 'axis', axisPointer: {type: 'shadow'}},
    xAxis: {data: xData, type: 'category', axisLabel: {rotate: 30, color: '#2F4F4F'}, axisLine: {lineStyle: {color: '#2F4F4F'}}},
    yAxis: {type: 'value', axisLine: {show: false}, axisTick: {show: false}, splitLine: {show: true, lineStyle: {color: 'rgba(10,10,10,0.1)', width: 1, type: 'solid'}}},
    series: [{data: yData, type: 'bar', itemStyle: {normal: {color: '#70B0EA'}}}]
  });
};

const initPieChart = () => {
  let pieData: any[] = [];
  tdata.data.popularClassification?.forEach((item: any) => {
    pieData.push({name: item.title, value: item.count});
  });
  pieChart = echarts.init(pieChartDiv.value);
  const colorList = ['#70B0EA', '#B3A3DA', '#88DEE2', '#62C4C8', '#58A3A1'];
  pieChart.setOption({
    grid: {top: '40px', left: '40px', right: '40px', bottom: '40px'},
    title: {text: '热门商品分类', textStyle: {color: '#aaa', fontSize: 18}, x: 'center', y: 'top'},
    tooltip: {trigger: 'item'},
    legend: {top: '90%', left: 'center'},
    series: [{
      name: '分类', type: 'pie', radius: ['40%', '70%'], avoidLabelOverlap: false,
      itemStyle: {normal: {color: (params: any) => colorList[params.dataIndex % colorList.length]}},
      label: {show: false, position: 'center'},
      emphasis: {label: {show: true, fontSize: 20, fontWeight: 'bold'}},
      labelLine: {show: false}, data: pieData
    }]
  });
};
</script>
```

- [ ] **Step 3: 添加 CSS 样式**

将 `<style>` 部分替换为：

```css
<style lang="less" scoped>
.main {
  height: 100%;
  display: flex;
  gap: 16px;
  flex-direction: column;
  padding: 16px;
  background: #f0f2f5;

  .box {
    padding: 12px;
    display: flex;
    flex-direction: column;

    .box-top {
      display: flex;
      flex-direction: row;
      align-items: center;
    }

    .box-value {
      color: #000;
      font-size: 32px;
      margin-right: 12px;

      .v-e {
        font-size: 14px;
      }
    }

    .box-bottom {
      margin-top: 24px;
      color: #000000d9;
    }
  }

  .chart-card {
    background: #fff;
    border-radius: 4px;
  }

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 8px 0;
    padding-left: 8px;
    border-left: 4px solid #1890ff;
  }
}
</style>
```

---

## 验证方式

1. **订单合并测试**：
   - 购物车选中多个商品，点击提交订单
   - 查看 `b_order` 表，验证是否只有一条记录（同一 orderNumber）
   - 或查看数据库中多条记录的 orderNumber 是否相同

2. **图表展示测试**：
   - 访问 `http://localhost:3000/#/admin/overview`
   - 验证页面是否有 5 个分析区块（用户/销售/订单/商品 + 保留图表）
   - 验证各图表是否正常渲染数据
   - 测试日/周/月切换按钮是否生效

3. **API 接口测试**：
   - 访问 `http://localhost:9100/api/swagger-ui/index.html`
   - 测试新增的 8 个接口是否正常返回数据
