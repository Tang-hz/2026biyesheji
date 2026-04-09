package com.gk.study.service;

import com.gk.study.entity.PointsLog;
import com.gk.study.entity.SignRecord;

import java.math.BigDecimal;
import java.util.List;

public interface PointsService {

    /**
     * 获取用户当前积分余额
     */
    int getUserPoints(Long userId);

    /**
     * 增加积分
     */
    void earnPoints(Long userId, Integer points, String type, Long orderId, String remark);

    /**
     * 抵扣积分（需校验余额充足）
     * @return 实际抵扣的积分数
     */
    int deductPoints(Long userId, Integer points, String remark);

    /**
     * 每日签到
     * @return 获得的积分数（含连续签到奖励）
     */
    int signIn(Long userId);

    /**
     * 判断积分是否足够抵扣指定金额
     */
    boolean canRedeem(BigDecimal orderAmount, Integer userPoints);

    /**
     * 计算积分可抵扣的金额
     */
    BigDecimal calcRedeemMoney(Integer points);

    /**
     * 获取用户积分记录明细
     */
    List<PointsLog> getPointsLog(Long userId);

    /**
     * 获取用户连续签到天数
     */
    int getConsecutiveSignDays(Long userId);

    /**
     * 检查今日是否已签到
     */
    boolean hasSignedToday(Long userId);
}
