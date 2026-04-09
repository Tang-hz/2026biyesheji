package com.gk.study.common;

/**
 * 积分规则常量
 */
public class PointsRule {

    /** 积分类型 */
    public static final String TYPE_ORDER = "ORDER";   // 购物获得
    public static final String TYPE_EVAL = "EVAL";     // 评价晒单
    public static final String TYPE_SIGN = "SIGN";     // 签到
    public static final String TYPE_REDEEM = "REDEEM"; // 抵扣

    /** 积分值 */
    public static final int POINTS_PER_YUAN = 1;        // 每消费1元得1积分
    public static final int EVAL_POINTS_MIN = 50;       // 评价晒单最少积分
    public static final int EVAL_POINTS_MAX = 100;       // 评价晒单最多积分
    public static final int SIGN_POINTS_MIN = 5;         // 每日签到最少积分
    public static final int SIGN_POINTS_MAX = 10;        // 每日签到最多积分
    public static final int SIGN_CONSECUTIVE_BONUS = 30; // 连续7天签到额外奖励

    /** 积分抵扣比例：100积分 = 1元 */
    public static final int POINTS_PER_MONEY = 100;

    private PointsRule() {}

    /**
     * 根据消费金额计算积分
     */
    public static int calcOrderPoints(int orderAmountYuan) {
        return orderAmountYuan;
    }

    /**
     * 积分能抵扣的金额
     */
    public static int calcRedeemMoney(int points) {
        return points / POINTS_PER_MONEY;
    }

    /**
     * 抵扣指定金额需要多少积分
     */
    public static int calcNeededPoints(int moneyYuan) {
        return moneyYuan * POINTS_PER_MONEY;
    }
}
