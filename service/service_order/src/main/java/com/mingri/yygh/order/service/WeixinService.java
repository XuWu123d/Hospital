package com.mingri.yygh.order.service;

import java.util.Map;

public interface WeixinService {
    //生成微信支付二维码
    Map createNative(Long orderId);

    //调用微信接口查询支付状态
    Map<String, String> getPayStatus(Long orderId);

    //退款
    boolean refund(Long orderId);
}
