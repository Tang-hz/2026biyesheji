package com.gk.study.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceUtils {

    private PriceUtils() {}

    /**
     * 元转分（避免浮点精度）
     */
    public static int yuanToFen(BigDecimal yuan) {
        if (yuan == null) return 0;
        return yuan.multiply(BigDecimal.valueOf(100)).intValue();
    }

    /**
     * 分转元
     */
    public static BigDecimal fenToYuan(int fen) {
        return BigDecimal.valueOf(fen).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 应用折扣，返回折后价
     * @param price 原价
     * @param discountRate 折扣率，如 0.98 表示98折
     */
    public static BigDecimal applyDiscount(BigDecimal price, BigDecimal discountRate) {
        if (price == null) return BigDecimal.ZERO;
        if (discountRate == null) discountRate = BigDecimal.ONE;
        return price.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 积分抵扣金额（100积分=1元）
     */
    public static BigDecimal pointsToMoney(int points) {
        if (points <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(points).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 金额转积分（1元=1积分）
     */
    public static int moneyToPoints(BigDecimal amount) {
        if (amount == null) return 0;
        return amount.intValue();
    }

    /**
     * 计算订单真实金额 = price * count
     */
    public static BigDecimal calcOrderItemTotal(BigDecimal price, int count) {
        if (price == null) return BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(count)).setScale(2, RoundingMode.HALF_UP);
    }
}
