package com.gk.study.ai.tool;

import com.gk.study.entity.Address;
import com.gk.study.entity.Order;
import com.gk.study.entity.Thing;
import com.gk.study.service.AddressService;
import com.gk.study.service.MemberService;
import com.gk.study.service.OrderService;
import com.gk.study.service.PointsService;
import com.gk.study.service.ThingService;
import com.gk.study.utils.PriceUtils;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * AI 客服工具（Function Calling）：下单并使用积分抵扣（原子操作）
 * 当用户下单并要求使用积分抵扣时调用此工具，确保积分扣减与下单在同一个事务中完成
 */
@Component
public class AiOrderRedeemTool {

    private final ThingService thingService;
    private final OrderService orderService;
    private final AddressService addressService;
    private final PointsService pointsService;
    private final MemberService memberService;

    public AiOrderRedeemTool(ThingService thingService,
                              OrderService orderService,
                              AddressService addressService,
                              PointsService pointsService,
                              MemberService memberService) {
        this.thingService = thingService;
        this.orderService = orderService;
        this.addressService = addressService;
        this.pointsService = pointsService;
        this.memberService = memberService;
    }

    @Tool("用户下单并使用积分抵扣时调用此工具。先计算积分抵扣金额，再创建订单并扣减积分，全自动完成。\n" +
          "当用户说「用xxx积分下单」「用积分兑换下单」「下单并用xxx积分抵扣」时必须调用此工具。\n" +
          "如果用户只说「帮我下单」且未提到积分，则调用 AiOrderTool 的普通下单工具（不要调用本工具）。")
    public String placeOrderWithRedeem(
            @P("商品主键 ID，纯数字；从用户提供的商品ID或检索结果中获取。")
            String thingId,

            @P(value = "数量，不提供则默认 1。", required = false)
            Integer count,

            @P("要使用积分抵扣的数量，单位：积分数（如用户说「用500积分」则填500）。注意：100积分=1元。")
            Integer redeemPoints,

            @P(value = "备注信息，可选。", required = false)
            String remark,

            @ToolMemoryId
            String userId
    ) {
        String uid = userId == null ? "" : userId.trim();
        if (uid.isBlank() || "guest".equalsIgnoreCase(uid)) {
            return "请先登录后再下单。";
        }

        // 解析商品
        if (thingId == null || thingId.isBlank()) {
            return "未提供商品ID，请提供商品ID后再下单。";
        }
        Thing thing = thingService.selectThingById(thingId);
        if (thing == null) {
            return "商品ID「" + thingId + "」在系统中不存在，请核对商品ID是否正确。";
        }

        // 检查收货地址
        java.util.List<Address> addresses = addressService.getAddressList(uid);
        if (addresses == null || addresses.isEmpty()) {
            return "你还没有收货地址。请先去【地址管理】新增地址后再下单。";
        }
        Address receiver = addresses.get(0);

        // 计算价格
        int quantity = (count == null || count < 1) ? 1 : count;
        BigDecimal itemPrice = thing.getPrice();
        BigDecimal subtotal = itemPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountRate = memberService.getMemberDiscount(Long.parseLong(uid));
        BigDecimal finalPrice = subtotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = subtotal.subtract(finalPrice);

        int usedPoints = 0;
        BigDecimal redeemMoney = BigDecimal.ZERO;
        if (redeemPoints != null && redeemPoints > 0) {
            // 检查积分是否足够
            int currentPoints = pointsService.getUserPoints(Long.parseLong(uid));
            if (currentPoints < redeemPoints) {
                return "您的积分余额为" + currentPoints + "积分，不足以抵扣" + redeemPoints + "积分。请减少抵扣积分数量或不使用积分抵扣。";
            }
            redeemMoney = PriceUtils.pointsToMoney(redeemPoints);
            finalPrice = finalPrice.subtract(redeemMoney);
            if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                finalPrice = BigDecimal.ZERO;
            }
            usedPoints = redeemPoints;
        }

        // 创建订单
        Order order = new Order();
        order.setThingId(String.valueOf(thing.getId()));
        order.setUserId(uid);
        order.setCount(String.valueOf(quantity));
        order.setReceiverName(receiver.getName());
        order.setReceiverPhone(receiver.getMobile());
        order.setReceiverAddress(receiver.getDescription());
        if (remark != null && !remark.trim().isEmpty()) {
            order.setRemark(remark.trim());
        }
        if (usedPoints > 0) {
            order.setRedeemPoints(usedPoints);
        }

        orderService.createOrder(order);

        if (order.getOrderNumber() == null || order.getOrderNumber().isBlank()) {
            return "下单成功，但订单号生成失败，请稍后重试。";
        }

        // 构造返回消息
        StringBuilder result = new StringBuilder();
        result.append("已为你下单《").append(thing.getTitle()).append("》，订单号：").append(order.getOrderNumber()).append("！\n\n");
        result.append("📦 **订单明细**\n");
        result.append("- 原价：¥").append(subtotal).append("\n");
        result.append("- 会员折扣：-¥").append(discountAmount).append("\n");
        result.append("- 折后价：¥").append(finalPrice.add(redeemMoney)).append("\n");
        if (usedPoints > 0) {
            result.append("- 积分抵扣：-¥").append(redeemMoney).append("（使用").append(usedPoints).append("积分）\n");
        }
        result.append("- 💵 **实付金额：¥").append(finalPrice).append("**\n");
        result.append("- 获得积分：约").append(finalPrice.intValue()).append("积分\n\n");
        if (usedPoints > 0) {
            int remainingPoints = pointsService.getUserPoints(Long.parseLong(uid));
            result.append("✅ 已使用").append(usedPoints).append("积分抵扣").append(redeemMoney).append("元，");
            result.append("当前剩余积分：**").append(remainingPoints).append("**");
        }

        return result.toString();
    }
}
