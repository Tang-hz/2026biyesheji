package com.gk.study.controller;

import com.gk.study.common.APIResponse;
import com.gk.study.common.ResponeCode;
import com.gk.study.entity.PointsLog;
import com.gk.study.permission.Access;
import com.gk.study.permission.AccessLevel;
import com.gk.study.service.PointsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "积分管理控制层")
@RestController
@RequestMapping("/points")
public class PointsController {

    @Autowired
    private PointsService pointsService;

    @Operation(summary = "获取当前积分")
    @Access(level = AccessLevel.LOGIN)
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public APIResponse getUserPoints(String userId) {
        int points = pointsService.getUserPoints(Long.parseLong(userId));
        return new APIResponse(ResponeCode.SUCCESS, "查询成功", points);
    }

    @Operation(summary = "每日签到")
    @Access(level = AccessLevel.LOGIN)
    @RequestMapping(value = "/sign", method = RequestMethod.POST)
    public APIResponse signIn(String userId) {
        // 检查今日是否已签到
        if (pointsService.hasSignedToday(Long.parseLong(userId))) {
            return new APIResponse(ResponeCode.FAIL, "今日已签到");
        }
        int earnedPoints = pointsService.signIn(Long.parseLong(userId));
        if (earnedPoints > 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("earnedPoints", earnedPoints);
            result.put("totalPoints", pointsService.getUserPoints(Long.parseLong(userId)));
            return new APIResponse(ResponeCode.SUCCESS, "签到成功", result);
        }
        return new APIResponse(ResponeCode.FAIL, "签到失败");
    }

    @Operation(summary = "积分抵扣")
    @Access(level = AccessLevel.LOGIN)
    @RequestMapping(value = "/redeem", method = RequestMethod.POST)
    public APIResponse redeem(String userId, Integer points, String remark) {
        if (points == null || points <= 0) {
            return new APIResponse(ResponeCode.FAIL, "抵扣积分必须大于0");
        }
        int deducted = pointsService.deductPoints(Long.parseLong(userId), points, remark);
        if (deducted > 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("deductedPoints", deducted);
            result.put("redeemMoney", pointsService.calcRedeemMoney(deducted));
            result.put("remainingPoints", pointsService.getUserPoints(Long.parseLong(userId)));
            return new APIResponse(ResponeCode.SUCCESS, "抵扣成功", result);
        }
        return new APIResponse(ResponeCode.FAIL, "积分不足或抵扣失败");
    }

    @Operation(summary = "获取积分记录明细")
    @Access(level = AccessLevel.LOGIN)
    @RequestMapping(value = "/log", method = RequestMethod.GET)
    public APIResponse getPointsLog(String userId) {
        List<PointsLog> logs = pointsService.getPointsLog(Long.parseLong(userId));
        return new APIResponse(ResponeCode.SUCCESS, "查询成功", logs);
    }

    @Operation(summary = "检查是否已签到")
    @Access(level = AccessLevel.LOGIN)
    @RequestMapping(value = "/signed", method = RequestMethod.GET)
    public APIResponse hasSignedToday(String userId) {
        boolean signed = pointsService.hasSignedToday(Long.parseLong(userId));
        Map<String, Object> result = new HashMap<>();
        result.put("signed", signed);
        if (!signed) {
            result.put("consecutiveDays", pointsService.getConsecutiveSignDays(Long.parseLong(userId)));
        }
        return new APIResponse(ResponeCode.SUCCESS, "查询成功", result);
    }
}
