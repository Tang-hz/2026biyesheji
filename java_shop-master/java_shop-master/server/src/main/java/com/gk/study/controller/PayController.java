package com.gk.study.controller;

import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import com.gk.study.entity.Order;
import com.gk.study.service.AlipayService;
import com.gk.study.service.MemberService;
import com.gk.study.service.OrderService;
import com.gk.study.service.PointsService;
import com.gk.study.common.PointsRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Tag(name = "支付管理控制层")
@RestController
@RequestMapping("/pay")
public class PayController {

    private static final Logger logger = LoggerFactory.getLogger(PayController.class);

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private PointsService pointsService;

    /**
     * 创建支付宝支付
     * @param orderNumber 订单号
     * @return 支付宝支付页面HTML
     */
    @Operation(summary = "创建支付宝支付")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public APIResponse createPay(@RequestParam String orderNumber) {
        try {
            // 查询订单
            List<Order> orders = orderService.getOrderListByOrderNumber(orderNumber);
            if (orders == null || orders.isEmpty()) {
                return new APIResponse(ResponeCode.FAIL, "订单不存在");
            }

            // 计算总金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            String subject = orders.get(0).getTitle();
            if (orders.size() > 1) {
                subject = "商城订单（共" + orders.size() + "件商品）";
            }
            for (Order order : orders) {
                totalAmount = totalAmount.add(order.getTotalPrice());
            }

            // 调用支付宝创建支付页面
            String form = alipayService.createPayPage(orderNumber, totalAmount.toPlainString(), subject);

            Map<String, Object> result = new HashMap<>();
            result.put("form", form);
            return new APIResponse(ResponeCode.SUCCESS, "创建成功", result);
        } catch (Exception e) {
            logger.error("创建支付宝支付失败", e);
            return new APIResponse(ResponeCode.FAIL, "创建支付失败: " + e.getMessage());
        }
    }

    /**
     * 支付宝异步回调
     */
    @RequestMapping(value = "/alipay/notify", method = RequestMethod.POST)
    @Transactional
    public void alipayNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String[]> requestParams = request.getParameterMap();
        Map<String, String> params = new HashMap<>();
        Iterator<String> iter = requestParams.keySet().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        logger.info("收到支付宝回调, params={}", params);

        // 验签
        boolean verifyResult = alipayService.verifyNotify(params);
        if (verifyResult) {
            String tradeStatus = params.get("trade_status");
            String orderNumber = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String totalAmount = params.get("total_amount");

            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 更新订单状态
                List<Order> orders = orderService.getOrderListByOrderNumber(orderNumber);
                if (orders != null && !orders.isEmpty()) {
                    for (Order order : orders) {
                        if (!"2".equals(order.getStatus())) {
                            order.setStatus("2"); // 已支付
                            order.setPayTime(LocalDateTime.now());
                            order.setTradeNo(tradeNo);
                            orderService.updateOrder(order);

                            // 处理积分
                            Long userId = Long.parseLong(order.getUserId());
                            int earnedPoints = order.getTotalPrice().intValue();
                            if (earnedPoints > 0) {
                                pointsService.earnPoints(userId, earnedPoints, PointsRule.TYPE_ORDER,
                                        order.getId(), "购物获得积分");
                            }
                        }
                    }

                    // 检查会员升级
                    Order firstOrder = orders.get(0);
                    Long userId = Long.parseLong(firstOrder.getUserId());
                    BigDecimal totalPaid = BigDecimal.ZERO;
                    for (Order order : orders) {
                        totalPaid = totalPaid.add(order.getTotalPrice());
                    }
                    memberService.checkAndUpgrade(userId, totalPaid);
                }
                logger.info("支付宝回调处理成功, orderNumber={}", orderNumber);
            }
            response.getWriter().print("success");
        } else {
            logger.warn("支付宝回调验签失败");
            response.getWriter().print("fail");
        }
    }

    /**
     * 查询支付状态（前端轮询）
     * @param orderNumber 订单号
     * @return 支付状态
     */
    @Operation(summary = "查询支付状态")
    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public APIResponse queryPayStatus(@RequestParam String orderNumber) {
        try {
            // 先查订单状态
            List<Order> orders = orderService.getOrderListByOrderNumber(orderNumber);
            if (orders == null || orders.isEmpty()) {
                return new APIResponse(ResponeCode.FAIL, "订单不存在");
            }

            // 如果已支付，直接返回成功
            boolean allPaid = true;
            for (Order order : orders) {
                if (!"2".equals(order.getStatus())) {
                    allPaid = false;
                    break;
                }
            }
            if (allPaid) {
                Map<String, Object> result = new HashMap<>();
                result.put("status", "TRADE_SUCCESS");
                result.put("paid", true);
                return new APIResponse(ResponeCode.SUCCESS, "支付成功", result);
            }

            // 调用支付宝查询
            String tradeStatus = alipayService.queryTradeStatus(orderNumber);
            if (tradeStatus == null) {
                tradeStatus = "WAIT_BUYER_PAY";
            }

            // 如果支付宝确认支付成功，更新订单状态
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                for (Order order : orders) {
                    if (!"2".equals(order.getStatus())) {
                        order.setStatus("2");
                        order.setPayTime(java.time.LocalDateTime.now());
                        orderService.updateOrder(order);

                        // 处理积分
                        Long userId = Long.parseLong(order.getUserId());
                        int earnedPoints = order.getTotalPrice().intValue();
                        if (earnedPoints > 0) {
                            pointsService.earnPoints(userId, earnedPoints, PointsRule.TYPE_ORDER,
                                    order.getId(), "购物获得积分");
                        }
                    }
                }

                // 检查会员升级
                Order firstOrder = orders.get(0);
                Long userId = Long.parseLong(firstOrder.getUserId());
                BigDecimal totalPaid = BigDecimal.ZERO;
                for (Order order : orders) {
                    totalPaid = totalPaid.add(order.getTotalPrice());
                }
                memberService.checkAndUpgrade(userId, totalPaid);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("status", tradeStatus);
            result.put("paid", "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus));
            return new APIResponse(ResponeCode.SUCCESS, "查询成功", result);
        } catch (Exception e) {
            logger.error("查询支付状态失败", e);
            return new APIResponse(ResponeCode.FAIL, "查询失败: " + e.getMessage());
        }
    }
}
