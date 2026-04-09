package com.gk.study.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gk.study.common.PointsRule;
import com.gk.study.entity.PointsLog;
import com.gk.study.entity.SignRecord;
import com.gk.study.entity.User;
import com.gk.study.mapper.PointsLogMapper;
import com.gk.study.mapper.SignRecordMapper;
import com.gk.study.mapper.UserMapper;
import com.gk.study.service.PointsService;
import com.gk.study.utils.PriceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class PointsServiceImpl extends ServiceImpl<PointsLogMapper, PointsLog> implements PointsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SignRecordMapper signRecordMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public int getUserPoints(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getScore() == null) {
            return 0;
        }
        try {
            return Integer.parseInt(user.getScore());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    @Transactional
    public void earnPoints(Long userId, Integer points, String type, Long orderId, String remark) {
        if (points == null || points <= 0) return;

        // 增加用户积分
        User user = userMapper.selectById(userId);
        int currentPoints = 0;
        if (user != null && user.getScore() != null) {
            try {
                currentPoints = Integer.parseInt(user.getScore());
            } catch (NumberFormatException ignored) {}
        }
        user.setScore(String.valueOf(currentPoints + points));
        userMapper.updateById(user);

        // 记录积分日志
        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setPoints(points);
        log.setType(type);
        log.setOrderId(orderId);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        baseMapper.insert(log);
    }

    @Override
    @Transactional
    public int deductPoints(Long userId, Integer points, String remark) {
        if (points == null || points <= 0) return 0;

        int currentPoints = getUserPoints(userId);
        if (currentPoints < points) {
            return 0; // 积分不足
        }

        // 扣减用户积分
        User user = userMapper.selectById(userId);
        user.setScore(String.valueOf(currentPoints - points));
        userMapper.updateById(user);

        // 记录积分日志（负数表示消耗）
        PointsLog log = new PointsLog();
        log.setUserId(userId);
        log.setPoints(-points);
        log.setType(PointsRule.TYPE_REDEEM);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        baseMapper.insert(log);

        return points;
    }

    @Override
    @Transactional
    public int signIn(Long userId) {
        // 检查今日是否已签到
        if (hasSignedToday(userId)) {
            return 0;
        }

        // 计算连续签到天数
        int consecutiveDays = getConsecutiveSignDays(userId);

        // 签到积分 (5-10随机 + 连续7天额外30)
        int signPoints = (int) (Math.random() * 6) + PointsRule.SIGN_POINTS_MIN; // 5-10
        if (consecutiveDays > 0 && consecutiveDays % 7 == 0) {
            signPoints += PointsRule.SIGN_CONSECUTIVE_BONUS;
        }

        // 记录签到
        SignRecord record = new SignRecord();
        record.setUserId(userId);
        record.setSignDate(LocalDate.now().format(DATE_FORMATTER));
        record.setPoints(signPoints);
        record.setCreateTime(LocalDateTime.now());
        signRecordMapper.insert(record);

        // 增加积分
        earnPoints(userId, signPoints, PointsRule.TYPE_SIGN, null, "每日签到");

        return signPoints;
    }

    @Override
    public boolean canRedeem(BigDecimal orderAmount, Integer userPoints) {
        if (orderAmount == null || userPoints == null) return false;
        int redeemMoney = calcRedeemMoney(userPoints).intValue();
        return redeemMoney > 0 && redeemMoney <= orderAmount.intValue();
    }

    @Override
    public BigDecimal calcRedeemMoney(Integer points) {
        if (points == null || points <= 0) return BigDecimal.ZERO;
        return PriceUtils.pointsToMoney(points);
    }

    @Override
    public List<PointsLog> getPointsLog(Long userId) {
        QueryWrapper<PointsLog> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        return baseMapper.selectList(wrapper);
    }

    @Override
    public int getConsecutiveSignDays(Long userId) {
        QueryWrapper<SignRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("sign_date");
        wrapper.last("LIMIT 7");
        List<SignRecord> records = signRecordMapper.selectList(wrapper);

        if (records.isEmpty()) return 0;

        int consecutiveDays = 0;
        LocalDate expectedDate = LocalDate.now().minusDays(1);

        for (SignRecord record : records) {
            LocalDate signDate = LocalDate.parse(record.getSignDate(), DATE_FORMATTER);
            if (signDate.equals(expectedDate)) {
                consecutiveDays++;
                expectedDate = expectedDate.minusDays(1);
            } else if (signDate.equals(expectedDate.plusDays(1)) && consecutiveDays == 0) {
                // 今天还没签（如果查出来的是今天的记录，说明已签）
                // 这种情况在 hasSignedToday 为 false 时不会进来
                expectedDate = signDate.minusDays(1);
                consecutiveDays = 1;
            } else {
                break;
            }
        }
        return consecutiveDays;
    }

    @Override
    public boolean hasSignedToday(Long userId) {
        QueryWrapper<SignRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("sign_date", LocalDate.now().format(DATE_FORMATTER));
        return signRecordMapper.selectCount(wrapper) > 0;
    }
}
