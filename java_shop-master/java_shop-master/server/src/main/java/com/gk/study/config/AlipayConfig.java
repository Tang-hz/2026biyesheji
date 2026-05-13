package com.gk.study.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {
    private String appId;
    private String gateway;
    private String privateKey;
    private String alipayPublicKey;
    private String notifyUrl;
    private String returnUrl;
}
