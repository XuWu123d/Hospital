package com.mingri.yygh.order.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 获取配置文件中的微信的值
 */
@Component
public class ConstantPropertiesUtils implements InitializingBean {
    @Value("${weixin.appid}")
    private String appid;
    @Value("${weixin.partner}")
    private String partner;
    @Value("${weixin.partnerkey}")
    private String partnerkey;
    @Value("${weixin.cert}")
    private String cert;

    public static String APP_ID;
    public static String PARTNER;
    public static String PARTNER_KEY;
    public static String CERT;

    @Override
    public void afterPropertiesSet() throws Exception {
        APP_ID=appid;
        PARTNER=partner;
        PARTNER_KEY=partnerkey;
        CERT=cert;
    }
}
