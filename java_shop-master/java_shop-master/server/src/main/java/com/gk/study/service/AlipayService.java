package com.gk.study.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.gk.study.config.AlipayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AlipayService {

    private static final Logger logger = LoggerFactory.getLogger(AlipayService.class);

    @Autowired
    private AlipayConfig alipayConfig;

    private AlipayClient getAlipayClient() {
        return new DefaultAlipayClient(
                alipayConfig.getGateway(),
                alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(),
                "json",
                "UTF-8",
                alipayConfig.getAlipayPublicKey(),
                "RSA2"
        );
    }

    /**
     * 创建电脑网站支付页面
     * @param orderNumber 订单号
     * @param amount 支付金额
     * @param subject 订单标题
     * @return 支付页面HTML（form表单自动提交）
     */
    public String createPayPage(String orderNumber, String amount, String subject) throws AlipayApiException {
        AlipayClient alipayClient = getAlipayClient();
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(alipayConfig.getReturnUrl());

        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(orderNumber);
        model.setTotalAmount(amount);
        model.setSubject(subject);
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        request.setBizModel(model);

        String form = alipayClient.pageExecute(request).getBody();
        logger.info("支付宝支付页面创建成功, orderNumber={}", orderNumber);
        return form;
    }

    /**
     * 验证支付宝异步回调签名
     * @param params 回调参数
     * @return 验证结果
     */
    public boolean verifyNotify(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(params,
                    alipayConfig.getAlipayPublicKey(), "UTF-8", "RSA2");
        } catch (AlipayApiException e) {
            logger.error("支付宝回调验签失败", e);
            return false;
        }
    }

    /**
     * 查询交易状态
     * @param orderNumber 商户订单号
     * @return 交易状态: TRADE_SUCCESS=交易成功, WAIT_BUYER_PAY=等待付款, TRADE_CLOSED=交易关闭
     */
    public String queryTradeStatus(String orderNumber) throws AlipayApiException {
        AlipayClient alipayClient = getAlipayClient();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(orderNumber);
        request.setBizModel(model);

        AlipayTradeQueryResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            return response.getTradeStatus();
        }
        logger.warn("支付宝查询交易状态失败: {}", response.getSubMsg());
        return null;
    }
}
