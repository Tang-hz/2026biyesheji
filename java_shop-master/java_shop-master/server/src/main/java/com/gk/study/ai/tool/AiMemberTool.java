package com.gk.study.ai.tool;

import com.gk.study.common.MemberLevel;
import com.gk.study.common.PointsRule;
import com.gk.study.service.MemberService;
import com.gk.study.service.PointsService;
import com.gk.study.utils.PriceUtils;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * AI 客服工具（Function Calling）：会员等级、积分余额、购买优惠计算
 */
@Component
public class AiMemberTool {

    private final MemberService memberService;
    private final PointsService pointsService;

    public AiMemberTool(MemberService memberService, PointsService pointsService) {
        this.memberService = memberService;
        this.pointsService = pointsService;
    }

    @Tool("查询用户当前会员等级、折扣率、累计消费、距下一等级门槛。")
    public String getMemberInfo(@ToolMemoryId String userId) {
        String uid = sanitize(userId);
        if (uid.isBlank() || "guest".equalsIgnoreCase(uid)) {
            return "查询会员信息需要您先登录账号。";
        }

        try {
            Long uidLong = Long.parseLong(uid);
            int level = memberService.getMemberLevel(uidLong);
            BigDecimal discountRate = memberService.getMemberDiscount(uidLong);
            BigDecimal threshold = memberService.getNextLevelThreshold(uidLong);

            String levelName = MemberLevel.getLevelName(level);
            String discountText = discountRate.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).toString();

            StringBuilder sb = new StringBuilder();
            sb.append("您当前是").append(levelName).append("，享受").append(discountText).append("折优惠。");

            if (threshold != null && threshold.compareTo(BigDecimal.ZERO) > 0) {
                sb.append("距升级还差 ¥").append(threshold.setScale(2, RoundingMode.HALF_UP)).append("消费额。");
                if (level == MemberLevel.SILVER) {
                    sb.append("升级后可享9折+生日礼券。");
                } else if (level == MemberLevel.GOLD) {
                    sb.append("升级后可享8.5折+优先发货+年度定制礼品。");
                }
            } else if (level >= MemberLevel.DIAMOND) {
                sb.append("您已是最高等级会员，享8.5折专属优惠，感谢一路陪伴！");
            }

            if (level >= MemberLevel.GOLD) {
                sb.append("您已享有全店免运费特权。");
            }

            return sb.toString();
        } catch (NumberFormatException e) {
            return "无法识别您的账号信息，请重新登录后尝试。";
        }
    }

    @Tool("查询用户当前积分余额和可抵扣金额。")
    public String getPointsInfo(@ToolMemoryId String userId) {
        String uid = sanitize(userId);
        if (uid.isBlank() || "guest".equalsIgnoreCase(uid)) {
            return "查询积分信息需要您先登录账号。";
        }

        try {
            Long uidLong = Long.parseLong(uid);
            int points = pointsService.getUserPoints(uidLong);
            BigDecimal redeemMoney = PriceUtils.pointsToMoney(points);

            StringBuilder sb = new StringBuilder();
            sb.append("您当前有 ").append(points).append(" 积分。");
            sb.append("按100积分=1元汇率，最多可抵扣 ¥").append(redeemMoney).append("。");

            // 提示积分来源
            sb.append("积分可通过购物、签到、评价获得。");

            return sb.toString();
        } catch (NumberFormatException e) {
            return "无法识别您的账号信息，请重新登录后尝试。";
        }
    }

    @Tool("根据商品单价和数量，计算用户购买时可享受的会员折扣、折后价、可用积分抵扣、实付金额以及可获得的积分。")
    public String calculatePurchaseBenefit(
            @P("商品单价（元）") BigDecimal price,
            @P("购买数量") Integer count,
            @ToolMemoryId String userId
    ) {
        String uid = sanitize(userId);
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return "请提供有效的商品价格。";
        }
        int qty = (count == null || count < 1) ? 1 : count;

        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);

        // 未登录时只展示通用规则
        if (uid.isBlank() || "guest".equalsIgnoreCase(uid)) {
            return "登录后可查看您的专属折扣价。商品原价 ¥" + subtotal + "，普通会员98折后约 ¥" +
                    subtotal.multiply(MemberLevel.NORMAL_DISCOUNT).setScale(2, RoundingMode.HALF_UP) +
                    "，实际价格以登录后为准。";
        }

        try {
            Long uidLong = Long.parseLong(uid);
            int level = memberService.getMemberLevel(uidLong);
            BigDecimal discountRate = memberService.getMemberDiscount(uidLong);
            BigDecimal finalPrice = subtotal.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal discountAmount = subtotal.subtract(finalPrice).setScale(2, RoundingMode.HALF_UP);
            int earnedPoints = finalPrice.intValue();
            int userPoints = pointsService.getUserPoints(uidLong);
            BigDecimal maxRedeemMoney = PriceUtils.pointsToMoney(userPoints);
            int maxRedeemPoints = Math.min(userPoints, earnedPoints * 100); // 最多抵扣不超过商品价格对应的积分

            String levelName = MemberLevel.getLevelName(level);
            String discountText = discountRate.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).toString();

            StringBuilder sb = new StringBuilder();
            sb.append("【购买 ").append(qty).append(" 件，单价 ¥").append(price).append("】\n");
            sb.append("原价合计：¥").append(subtotal).append("\n");
            sb.append("会员折扣（").append(levelName).append("）").append(discountText).append("折：-¥").append(discountAmount).append("\n");
            sb.append("折后价：¥").append(finalPrice).append("\n");
            sb.append("本单可获得积分：").append(earnedPoints).append("\n");

            if (userPoints > 0 && maxRedeemMoney.compareTo(BigDecimal.ZERO) > 0) {
                sb.append("您有 ").append(userPoints).append(" 积分，最多可抵扣 ¥").append(maxRedeemMoney).append("。");
                // 展示使用积分后的实付金额
                BigDecimal afterRedeem = finalPrice.subtract(maxRedeemMoney).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
                sb.append("使用全部积分后实付约 ¥").append(afterRedeem).append("。");
            } else {
                sb.append("您当前暂无积分，签到或购物后可获得积分，下次即可抵扣。");
            }

            return sb.toString();
        } catch (NumberFormatException e) {
            return "无法识别您的账号信息，请重新登录后尝试。";
        }
    }

    private String sanitize(String s) {
        return (s == null) ? "" : s.trim();
    }
}
