package com.gk.study.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gk.study.common.MemberLevel;
import com.gk.study.common.PointsRule;
import com.gk.study.entity.Order;
import com.gk.study.entity.Thing;
import com.gk.study.mapper.OrderMapper;
import com.gk.study.mapper.ThingMapper;
import com.gk.study.service.MemberService;
import com.gk.study.service.OrderService;
import com.gk.study.service.PointsService;
import com.gk.study.utils.PriceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    @Autowired
    OrderMapper mapper;

    @Autowired
    ThingMapper thingMapper;

    @Autowired
    MemberService memberService;

    @Autowired
    PointsService pointsService;

    @Override
    public List<Order> getOrderList() {
        return mapper.getList();
    }

    @Override
    @Transactional
    public void createOrder(Order order, List<Order.ThingItem> items) {

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
                    itemRedeemPoints = itemRedeemMoney.multiply(BigDecimal.valueOf(100)).intValue();
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

    @Override
    public void deleteOrder(String id) {
        mapper.deleteById(id);
    }

    @Override
    public void updateOrder(Order order) {
        mapper.updateById(order);
    }

    @Override
    public List<Order> getUserOrderList(String userId, String status) {
        return mapper.getUserOrderList(userId, status);
    }
}
