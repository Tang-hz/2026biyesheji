package com.gk.study.controller;

import com.gk.study.common.APIResponse;
import com.gk.study.common.MemberLevel;
import com.gk.study.common.ResponeCode;
import com.gk.study.permission.Access;
import com.gk.study.permission.AccessLevel;
import com.gk.study.service.MemberService;
import com.gk.study.service.PointsService;
import com.gk.study.service.ThingService;
import com.gk.study.utils.PriceUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "会员权益控制层")
@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private PointsService pointsService;

    @Autowired
    private ThingService thingService;

    @Operation(summary = "计算商品会员折扣价")
    @Access(level = AccessLevel.LOGIN)
    @GetMapping("/calcPrice")
    public APIResponse calcPrice(String thingId, Integer count, String userId) {
        if (thingId == null || thingId.isBlank()) {
            return new APIResponse(ResponeCode.FAIL, "商品ID不能为空");
        }
        int qty = (count == null || count < 1) ? 1 : count;

        Long uid = Long.parseLong(userId);
        int level = memberService.getMemberLevel(uid);
        BigDecimal discountRate = memberService.getMemberDiscount(uid);

        var thing = thingService.selectThingById(thingId);
        BigDecimal price = (thing == null || thing.getPrice() == null) ? BigDecimal.ZERO : thing.getPrice();
        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalPrice = subtotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = subtotal.subtract(finalPrice).setScale(2, RoundingMode.HALF_UP);
        int earnedPoints = finalPrice.intValue();
        int userPoints = pointsService.getUserPoints(uid);
        int maxRedeemPoints = Math.min(userPoints, earnedPoints * 100);
        BigDecimal maxRedeemMoney = PriceUtils.pointsToMoney(maxRedeemPoints);
        BigDecimal threshold = memberService.getNextLevelThreshold(uid);

        Map<String, Object> result = new HashMap<>();
        result.put("title", thing != null ? thing.getTitle() : "");
        result.put("price", price);
        result.put("count", qty);
        result.put("subtotal", subtotal);
        result.put("discountRate", discountRate);
        result.put("discountAmount", discountAmount);
        result.put("finalPrice", finalPrice);
        result.put("earnedPoints", earnedPoints);
        result.put("memberLevel", level);
        result.put("memberLevelName", MemberLevel.getLevelName(level));
        result.put("userPoints", userPoints);
        result.put("maxRedeemPoints", maxRedeemPoints);
        result.put("maxRedeemMoney", maxRedeemMoney);
        result.put("nextLevelThreshold", threshold);
        result.put("canFreeShipping", MemberLevel.canFreeShipping(level));

        return new APIResponse(ResponeCode.SUCCESS, "查询成功", result);
    }

    @Operation(summary = "获取会员信息")
    @Access(level = AccessLevel.LOGIN)
    @GetMapping("/info")
    public APIResponse getMemberInfo(String userId) {
        Long uid = Long.parseLong(userId);
        int level = memberService.getMemberLevel(uid);
        BigDecimal threshold = memberService.getNextLevelThreshold(uid);

        Map<String, Object> result = new HashMap<>();
        result.put("memberLevel", level);
        result.put("memberLevelName", MemberLevel.getLevelName(level));
        result.put("discountRate", memberService.getMemberDiscount(uid));
        result.put("nextLevelThreshold", threshold);
        result.put("canFreeShipping", MemberLevel.canFreeShipping(level));
        result.put("userPoints", pointsService.getUserPoints(uid));

        return new APIResponse(ResponeCode.SUCCESS, "查询成功", result);
    }
}
