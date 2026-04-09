package com.gk.study.common;

import java.math.BigDecimal;

/**
 * 会员等级常量
 * 普通会员: 9.8折, 门槛0元
 * 白银会员: 9.5折 + 生日礼券, 门槛1000元
 * 黄金会员: 9.0折 + 专属客服 + 免运费, 门槛5000元
 * 钻石会员: 8.5折 + 优先发货 + 年度定制礼品, 门槛20000元
 */
public class MemberLevel {

    public static final int NORMAL = 1;   // 普通会员
    public static final int SILVER = 2;   // 白银会员
    public static final int GOLD = 3;     // 黄金会员
    public static final int DIAMOND = 4;  // 钻石会员

    /** 累计消费门槛（元） */
    public static final BigDecimal SILVER_THRESHOLD = new BigDecimal("1000");
    public static final BigDecimal GOLD_THRESHOLD = new BigDecimal("5000");
    public static final BigDecimal DIAMOND_THRESHOLD = new BigDecimal("20000");

    /** 折扣率 */
    public static final BigDecimal NORMAL_DISCOUNT = new BigDecimal("0.98");
    public static final BigDecimal SILVER_DISCOUNT = new BigDecimal("0.95");
    public static final BigDecimal GOLD_DISCOUNT = new BigDecimal("0.90");
    public static final BigDecimal DIAMOND_DISCOUNT = new BigDecimal("0.85");

    private MemberLevel() {}

    /**
     * 根据会员等级获取折扣率
     */
    public static BigDecimal getDiscountRate(int level) {
        switch (level) {
            case SILVER:  return SILVER_DISCOUNT;
            case GOLD:    return GOLD_DISCOUNT;
            case DIAMOND: return DIAMOND_DISCOUNT;
            default:      return NORMAL_DISCOUNT;
        }
    }

    /**
     * 根据累计消费金额计算会员等级
     */
    public static int calculateLevel(BigDecimal cumulativeAmount) {
        if (cumulativeAmount == null) return NORMAL;
        if (cumulativeAmount.compareTo(DIAMOND_THRESHOLD) >= 0) return DIAMOND;
        if (cumulativeAmount.compareTo(GOLD_THRESHOLD) >= 0) return GOLD;
        if (cumulativeAmount.compareTo(SILVER_THRESHOLD) >= 0) return SILVER;
        return NORMAL;
    }

    /**
     * 等级是否享受免运费（黄金及以上）
     */
    public static boolean canFreeShipping(int level) {
        return level >= GOLD;
    }

    /**
     * 获取等级名称
     */
    public static String getLevelName(int level) {
        switch (level) {
            case SILVER:  return "白银会员";
            case GOLD:    return "黄金会员";
            case DIAMOND: return "钻石会员";
            default:      return "普通会员";
        }
    }
}
