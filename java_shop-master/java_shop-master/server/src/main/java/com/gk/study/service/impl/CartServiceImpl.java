package com.gk.study.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gk.study.common.MemberLevel;
import com.gk.study.entity.Cart;
import com.gk.study.mapper.CartMapper;
import com.gk.study.service.CartService;
import com.gk.study.service.MemberService;
import com.gk.study.service.PointsService;
import com.gk.study.utils.PriceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private MemberService memberService;

    @Autowired
    private PointsService pointsService;

    @Override
    public void addToCart(Cart in) {
        int n = in.getItemCount() == null || in.getItemCount() < 1 ? 1 : in.getItemCount();
        QueryWrapper<Cart> qw = new QueryWrapper<>();
        qw.eq("user_id", in.getUserId()).eq("thing_id", in.getThingId());
        Cart existing = cartMapper.selectOne(qw);
        if (existing != null) {
            existing.setItemCount(existing.getItemCount() + n);
            cartMapper.updateById(existing);
        } else {
            Cart c = new Cart();
            c.setUserId(in.getUserId());
            c.setThingId(in.getThingId());
            c.setItemCount(n);
            cartMapper.insert(c);
        }
    }

    @Override
    public void updateItemCount(String id, int itemCount) {
        if (itemCount < 1) {
            throw new IllegalArgumentException("数量至少为 1");
        }
        Cart c = cartMapper.selectById(id);
        if (c == null) {
            throw new IllegalArgumentException("购物车项不存在");
        }
        c.setItemCount(itemCount);
        cartMapper.updateById(c);
    }

    @Override
    public void remove(String id) {
        cartMapper.deleteById(id);
    }

    @Override
    public List<Map<String, Object>> listByUser(String userId) {
        List<Map<String, Object>> list = cartMapper.getCartList(userId);

        if (userId == null || userId.isBlank()) {
            return list;
        }

        try {
            Long uidLong = Long.parseLong(userId);
            BigDecimal discountRate = memberService.getMemberDiscount(uidLong);
            int userPoints = pointsService.getUserPoints(uidLong);

            BigDecimal totalSubtotal = BigDecimal.ZERO;
            BigDecimal totalFinalPrice = BigDecimal.ZERO;

            for (Map<String, Object> item : list) {
                // 计算原价小计
                BigDecimal price = parseDecimal(item.get("price"));
                int count = parseInt(item.get("count"));
                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(count)).setScale(2, RoundingMode.HALF_UP);

                // 计算折后价
                BigDecimal finalPrice = subtotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
                BigDecimal discountAmount = subtotal.subtract(finalPrice).setScale(2, RoundingMode.HALF_UP);

                item.put("subtotal", subtotal);
                item.put("discountRate", discountRate);
                item.put("finalPrice", finalPrice);
                item.put("discountAmount", discountAmount);
                item.put("earnedPoints", finalPrice.intValue());

                totalSubtotal = totalSubtotal.add(subtotal);
                totalFinalPrice = totalFinalPrice.add(finalPrice);
            }

            // 整单汇总
            BigDecimal totalDiscountAmount = totalSubtotal.subtract(totalFinalPrice).setScale(2, RoundingMode.HALF_UP);
            int totalEarnedPoints = totalFinalPrice.intValue();
            int maxRedeemPoints = Math.min(userPoints, totalEarnedPoints * 100); // 最多用订单积分2倍
            BigDecimal maxRedeemMoney = PriceUtils.pointsToMoney(maxRedeemPoints);

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalSubtotal", totalSubtotal);
            summary.put("totalDiscountAmount", totalDiscountAmount);
            summary.put("totalFinalPrice", totalFinalPrice);
            summary.put("totalEarnedPoints", totalEarnedPoints);
            summary.put("userPoints", userPoints);
            summary.put("maxRedeemPoints", maxRedeemPoints);
            summary.put("maxRedeemMoney", maxRedeemMoney);
            summary.put("memberLevel", memberService.getMemberLevel(uidLong));
            summary.put("memberLevelName", MemberLevel.getLevelName(memberService.getMemberLevel(uidLong)));
            summary.put("discountRate", discountRate);

            // 将汇总信息放入第一个元素的额外字段
            if (!list.isEmpty()) {
                list.get(0).put("_orderSummary", summary);
            }

        } catch (NumberFormatException e) {
            // ignore, return original list
        }

        return list;
    }

    @Override
    public void clearByUser(String userId) {
        QueryWrapper<Cart> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        cartMapper.delete(qw);
    }

    @Override
    public int sumItemCountByUser(String userId) {
        Long n = cartMapper.sumItemCountByUserId(userId);
        return n == null ? 0 : n.intValue();
    }

    private BigDecimal parseDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        try {
            return new BigDecimal(val.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private int parseInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Integer) return (Integer) val;
        if (val instanceof Long) return ((Long) val).intValue();
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
