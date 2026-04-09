package com.gk.study.service;

import java.math.BigDecimal;

public interface MemberService {

    /**
     * 获取用户会员等级
     */
    int getMemberLevel(Long userId);

    /**
     * 获取用户折扣率
     */
    BigDecimal getMemberDiscount(Long userId);

    /**
     * 判断用户是否享受免运费（黄金及以上）
     */
    boolean canFreeShipping(Long userId);

    /**
     * 检查并升级会员等级
     * @return 新的会员等级
     */
    int checkAndUpgrade(Long userId, BigDecimal newAmount);

    /**
     * 更新用户累计消费金额
     */
    void updateCumulativeAmount(Long userId, BigDecimal amount);
}
