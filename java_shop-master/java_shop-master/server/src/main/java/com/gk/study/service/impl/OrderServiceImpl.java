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
    public void createOrder(Order order) {
        // 获取商品信息
        Thing thing = thingMapper.selectById(order.getThingId());
        if (thing == null) {
            throw new RuntimeException("商品不存在");
        }

        // 计算商品小计 = 单价 * 数量
        BigDecimal itemPrice = thing.getPrice();
        if (itemPrice == null) {
            itemPrice = BigDecimal.ZERO;
        }
        int count = 1;
        try {
            count = Integer.parseInt(order.getCount());
        } catch (NumberFormatException ignored) {}

        BigDecimal subtotal = itemPrice.multiply(BigDecimal.valueOf(count))
                .setScale(2, RoundingMode.HALF_UP);

        // 获取会员折扣率
        Long userId = Long.parseLong(order.getUserId());
        BigDecimal discountRate = memberService.getMemberDiscount(userId);

        // 计算折后金额
        BigDecimal finalPrice = subtotal.multiply(discountRate)
                .setScale(2, RoundingMode.HALF_UP);

        // 积分抵扣（如果有）
        BigDecimal redeemMoney = BigDecimal.ZERO;
        int usedPoints = (order.getRedeemPoints() != null) ? order.getRedeemPoints() : 0;
        System.out.println("[OrderService] redeemPoints from order: " + order.getRedeemPoints() + ", usedPoints: " + usedPoints);
        if (usedPoints > 0) {
            redeemMoney = PriceUtils.pointsToMoney(usedPoints);
            finalPrice = finalPrice.subtract(redeemMoney);
            if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
                finalPrice = BigDecimal.ZERO;
            }
        }

        // 设置订单信息
        order.setOrderTime(java.time.LocalDateTime.now());
        order.setOrderNumber(String.valueOf(System.currentTimeMillis()));
        order.setStatus("2"); // 已支付
        order.setTotalPrice(finalPrice);

        mapper.insert(order);

        // 订单完成后：增加积分（按折后价计算，每消费1元得1积分）
        int earnedPoints = finalPrice.intValue();
        if (earnedPoints > 0) {
            pointsService.earnPoints(userId, earnedPoints, PointsRule.TYPE_ORDER,
                    order.getId(), "购物获得积分");
        }

        // 抵扣积分扣减（如有）
        if (usedPoints > 0) {
            System.out.println("[OrderService] calling deductPoints: userId=" + userId + ", points=" + usedPoints);
            int deducted = pointsService.deductPoints(userId, usedPoints, "订单抵扣");
            System.out.println("[OrderService] deductPoints result: " + deducted);
        }

        // 更新用户累计消费金额并检查会员升级（按实际支付额）
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
