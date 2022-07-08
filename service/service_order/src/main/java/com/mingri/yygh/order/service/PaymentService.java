package com.mingri.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.yygh.model.order.OrderInfo;
import com.mingri.yygh.model.order.PaymentInfo;

import java.util.Map;

public interface PaymentService extends IService<PaymentInfo> {
    //调用支付接口进行支付
    void paySuccess(String out_trade_no, Map<String,String> resultMap);

    //向支付记录表中添加信息
    void savePaymentInfo(OrderInfo order, Integer status);

    //获取支付记录
    PaymentInfo getPaymentInfo(Long orderId,Integer paymentType);
}
