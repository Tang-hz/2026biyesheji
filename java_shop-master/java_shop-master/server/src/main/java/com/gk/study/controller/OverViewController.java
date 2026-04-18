package com.gk.study.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sun.management.OperatingSystemMXBean;
import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import com.gk.study.entity.Order;
import com.gk.study.entity.Thing;
import com.gk.study.entity.User;
import com.gk.study.entity.VisitData;
import com.gk.study.mapper.OrderMapper;
import com.gk.study.mapper.OverviewMapper;
import com.gk.study.mapper.ThingMapper;
import com.gk.study.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统概览统计控制器
 * 负责处理系统数据统计和监控相关的 HTTP 请求
 * 提供系统信息、商品统计、订单统计、热门商品、流量分析等功能
 * 用于后台管理系统的首页数据展示和系统监控
 */
@Tag(name = "数据统计概览控制层")
@RestController
@RequestMapping("/overview")
public class OverViewController {

    @Autowired
    ThingMapper thingMapper;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    OverviewMapper overviewMapper;

    @Autowired
    UserMapper userMapper;

    private final static Logger logger = LoggerFactory.getLogger(OverViewController.class);

    /**
     * 获取系统信息
     * 查询服务器硬件信息、操作系统信息、JVM 信息、内存使用情况等
     * 
     * @return APIResponse 包含系统详细信息的响应对象
     */
    @Operation(summary = "服务器与 JVM 信息")
    @RequestMapping(value = "/sysInfo", method = RequestMethod.GET)
    public APIResponse sysInfo() {

        Map<String, String> map = new HashMap<>();
        map.put("sysName", "后台系统");
        map.put("versionName", "1.0");
        map.put("processor", "2");
        map.put("sysLan", "En");
        map.put("sysZone", "东八区");

        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        map.put("pf", osmxb.getArch());
        map.put("osName", osmxb.getName());
        map.put("cpuCount", String.valueOf(osmxb.getAvailableProcessors()));

        DecimalFormat df = new DecimalFormat("#,##0.0");

        //获取 CPU
        double cpuLoad = osmxb.getSystemCpuLoad();
        //获取内存
        double totalvirtualMemory = osmxb.getTotalPhysicalMemorySize();
        double freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize();
        double value = freePhysicalMemorySize / totalvirtualMemory;
        double percentMemoryLoad = ((1 - value) * 100);
        map.put("cpuLoad", df.format(cpuLoad * 100));
        map.put("memory", df.format(totalvirtualMemory / 1024 / 1024 / 1024));
        map.put("usedMemory", df.format((totalvirtualMemory - freePhysicalMemorySize) / 1024 / 1024 / 1024));
        map.put("percentMemory", df.format(percentMemoryLoad));

        map.put("jvmVersion", System.getProperty("java.version"));

        return new APIResponse(ResponeCode.SUCCESS, "查询成功", map);
    }

    /**
     * 获取统计数据
     * 查询商品总数、新增商品、各类订单统计、热门商品、热门分类、网站流量等综合数据
     * 
     * @return APIResponse 包含各类统计数据的响应对象
     */
    @Operation(summary = "后台统计数据（商品/订单/流量等）")
    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public APIResponse count() {


        Map<String, Object> map = new HashMap<>();

        // 商品总数
        QueryWrapper<Thing> queryWrapper = new QueryWrapper<>();
        long spzs = thingMapper.selectCount(queryWrapper);
        map.put("spzs", spzs);

        long now = System.currentTimeMillis();
        long sevenMillis = now - 7 * 24 * 60 * 60 * 1000; // 7 天前
        System.out.println(sevenMillis);

        // 七日新增
        queryWrapper.ge("create_time", sevenMillis);
        long qrxz = thingMapper.selectCount(queryWrapper);
        map.put("qrxz", qrxz);

        // 未付订单
        QueryWrapper<Order> orderQueryWrapper = new QueryWrapper<>();
        orderQueryWrapper.eq("status", "1");
        long wfdd = orderMapper.selectCount(orderQueryWrapper);
        map.put("wfdd", wfdd);

        // 未付订单人数
        orderQueryWrapper = new QueryWrapper<>();
        orderQueryWrapper.select("distinct user_id");
        orderQueryWrapper.eq("status", "1");
        orderQueryWrapper.groupBy("user_id");
        map.put("wfddrs", orderMapper.selectList(orderQueryWrapper).size());

        // 已付订单
        orderQueryWrapper = new QueryWrapper<>();
        orderQueryWrapper.eq("status", "2");
        long yfdd = orderMapper.selectCount(orderQueryWrapper);
        map.put("yfdd", yfdd);

        // 已付订单人数
        orderQueryWrapper = new QueryWrapper<>();
        orderQueryWrapper.select("distinct user_id");
        orderQueryWrapper.eq("status", "2");
        orderQueryWrapper.groupBy("user_id");
        map.put("yfddrs", orderMapper.selectList(orderQueryWrapper).size());

        // 取消订单
        orderQueryWrapper = new QueryWrapper<>();
        orderQueryWrapper.eq("status", "7");
        long qxdd = orderMapper.selectCount(orderQueryWrapper);
        map.put("qxdd", qxdd);

        // 取消订单人数
        orderQueryWrapper = new QueryWrapper<>();
        orderQueryWrapper.select("distinct user_id");
        orderQueryWrapper.eq("status", "7");
        orderQueryWrapper.groupBy("user_id");
        map.put("qxddrs", orderMapper.selectList(orderQueryWrapper).size());


        // 热门商品
        List<Object> popularThings = overviewMapper.getPopularThing();
        map.put("popularThings", popularThings);

        // 热门分类
        List<Object> popularClassification = overviewMapper.getPopularClassification();
        map.put("popularClassification", popularClassification);

        // 网站流量
        List<Object> visitList = new ArrayList<>();
        List<String> sevenList = getSevenDate();
        for(String day: sevenList){
            Map<String, String> visitMap = new HashMap<>();
            visitMap.put("day", day);
            int pv = 0;
            int uv = 0;
            List<VisitData> webVisitData = overviewMapper.getWebVisitData(day);
            for(VisitData visitData: webVisitData) {
                pv += visitData.count;
            }
            uv = webVisitData.size();
            visitMap.put("pv", String.valueOf(pv));
            visitMap.put("uv", String.valueOf(uv));
            visitList.add(visitMap);
        }
        map.put("visitList", visitList);

        return new APIResponse(ResponeCode.SUCCESS, "查询成功", map);
    }


    /**
     * 获取最近七天的日期
     * 生成从当天往前推 7 天的日期列表
     * 
     * @return 包含最近七天日期的列表（格式：yyyy-MM-dd）
     */
    public static List<String> getSevenDate() {

        List<String> dateList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (int i = 0; i < 7; i++) {

            Date date = DateUtils.addDays(new Date(), -i);
            String formatDate = sdf.format(date);
            dateList.add(formatDate);
        }
        Collections.reverse(dateList);
        return dateList;
    }

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
            qw.eq("status", "2");
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
            final int low = lows[i];
            final int high = highs[i];
            long count;
            if (high == Integer.MAX_VALUE) {
                count = orders.stream()
                        .filter(o -> o.getTotalPrice() != null &&
                                o.getTotalPrice().doubleValue() >= low)
                        .count();
            } else {
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
            for (int i = 3; i >= 0; i--) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.WEEK_OF_YEAR, -i);
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                dates.add(sdf.format(cal.getTime()));
            }
        } else if ("month".equals(type)) {
            for (int i = 5; i >= 0; i--) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, -i);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                dates.add(sdf.format(cal.getTime()));
            }
        } else {
            for (int i = 6; i >= 0; i--) {
                Date date = DateUtils.addDays(new Date(), -i);
                dates.add(sdf.format(date));
            }
        }
        return dates;
    }

}
