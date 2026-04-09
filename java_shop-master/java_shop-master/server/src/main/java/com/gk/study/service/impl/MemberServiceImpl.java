package com.gk.study.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gk.study.common.MemberLevel;
import com.gk.study.entity.User;
import com.gk.study.mapper.UserMapper;
import com.gk.study.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public int getMemberLevel(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getMemberLevel() == null) {
            return MemberLevel.NORMAL;
        }
        return user.getMemberLevel();
    }

    @Override
    public BigDecimal getMemberDiscount(Long userId) {
        int level = getMemberLevel(userId);
        return MemberLevel.getDiscountRate(level);
    }

    @Override
    public boolean canFreeShipping(Long userId) {
        return MemberLevel.canFreeShipping(getMemberLevel(userId));
    }

    @Override
    @Transactional
    public int checkAndUpgrade(Long userId, BigDecimal newAmount) {
        User user = userMapper.selectById(userId);
        if (user == null) return MemberLevel.NORMAL;

        // 累加消费金额
        BigDecimal cumulative = user.getCumulativeAmount();
        if (cumulative == null) {
            cumulative = BigDecimal.ZERO;
        }
        cumulative = cumulative.add(newAmount);
        user.setCumulativeAmount(cumulative);

        // 计算并升级会员等级
        int newLevel = MemberLevel.calculateLevel(cumulative);
        if (user.getMemberLevel() == null || newLevel > user.getMemberLevel()) {
            user.setMemberLevel(newLevel);
        }

        userMapper.updateById(user);
        return newLevel;
    }

    @Override
    @Transactional
    public void updateCumulativeAmount(Long userId, BigDecimal amount) {
        User user = userMapper.selectById(userId);
        if (user == null) return;

        BigDecimal cumulative = user.getCumulativeAmount();
        if (cumulative == null) {
            cumulative = BigDecimal.ZERO;
        }
        user.setCumulativeAmount(cumulative.add(amount));
        userMapper.updateById(user);
    }
}
