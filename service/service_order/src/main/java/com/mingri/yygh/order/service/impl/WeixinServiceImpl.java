package com.mingri.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.mingri.yygh.enums.PaymentTypeEnum;
import com.mingri.yygh.enums.RefundStatusEnum;
import com.mingri.yygh.model.order.OrderInfo;
import com.mingri.yygh.model.order.PaymentInfo;
import com.mingri.yygh.model.order.RefundInfo;
import com.mingri.yygh.order.service.OrderService;
import com.mingri.yygh.order.service.PaymentService;
import com.mingri.yygh.order.service.RefundService;
import com.mingri.yygh.order.service.WeixinService;
import com.mingri.yygh.order.utils.ConstantPropertiesUtils;
import com.mingri.yygh.order.utils.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WeixinServiceImpl implements WeixinService {
    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RefundService refundService;

    //生成微信支付二维码
    @Override
    public Map createNative(Long orderId) {
        try {
            //从redis中获取数据
            Map result =(Map) redisTemplate.opsForValue().get(orderId.toString());
            //不为空，表明redis中已经存在
            if (null!=result) {
                return result;
            }
            //根据orderId获取订单信息
            OrderInfo order = orderService.getById(orderId);
            //向支付记录表中添加信息
            paymentService.savePaymentInfo(order, PaymentTypeEnum.WEIXIN.getStatus());
            //设置参数
            Map paramMap=new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APP_ID);
            paramMap.put("mch_id",ConstantPropertiesUtils.PARTNER);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            String body=order.getReserveDate()+"就诊"+order.getDepname();
            paramMap.put("body",body);
            paramMap.put("out_trade_no",order.getOutTradeNo());
            paramMap.put("total_fee", "1");
            paramMap.put("spbill_create_ip", "127.0.0.1");
            paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
            paramMap.put("trade_type", "NATIVE");
            //2、HTTPClient来根据URL访问第三方接口并且传递参数
            String baseUrl="https://api.mch.weixin.qq.com/pay/unifiedorder";
            Map<String, String> resultMap = this.getWXInterface(paramMap, baseUrl, ConstantPropertiesUtils.PARTNER_KEY);
            System.out.println("resultMap"+resultMap);
            //封装返回结果集
            Map map=new HashMap<>();
            map.put("orderId",orderId);
            map.put("totalFee",order.getAmount());
            map.put("resultCode",resultMap.get("result_code"));
            map.put("codeUrl",resultMap.get("code_url"));  //二维码地址
            if (resultMap.get("result_code")!=null) {
                //微信支付二维码2小时过期，可采用2小时未支付取消订单
                redisTemplate.opsForValue().set(order.toString(),map,120, TimeUnit.MINUTES);
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    //调用微信接口查询支付状态
    @Override
    public Map<String, String> getPayStatus(Long orderId) {
        try {
            //调用订单接口获取订单信息
            OrderInfo order = orderService.getById(orderId);
            //封装提交参数
            Map paramMap=new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APP_ID);
            paramMap.put("mch_id",ConstantPropertiesUtils.PARTNER);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            paramMap.put("out_trade_no",order.getOutTradeNo());
            //2、HTTPClient来根据URL访问第三方接口并且传递参数
            String baseUrl="https://api.mch.weixin.qq.com/pay/orderquery";
            Map<String, String> resultMap = this.getWXInterface(paramMap, baseUrl, ConstantPropertiesUtils.PARTNER_KEY);

            //把接口数据返回
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //退款
    @Override
    public boolean refund(Long orderId) {
        try {
            System.out.println("调用");
            //获取支付信息
            PaymentInfo paymentInfo = paymentService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());
            //添加退款信息
            RefundInfo refundInfo =refundService.saveRefundInfo(paymentInfo);
            //判断是否已经退款
            if (refundInfo.getRefundStatus().intValue()== RefundStatusEnum.REFUND.getStatus().intValue()) {
                return true;
            }
            //需要进行退款
            Map<String,String> paramMap=new HashMap<>();
            paramMap.put("appid", ConstantPropertiesUtils.APP_ID);
            paramMap.put("mch_id",ConstantPropertiesUtils.PARTNER);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            paramMap.put("transaction_id",paymentInfo.getTradeNo());
            paramMap.put("out_trade_no",paymentInfo.getOutTradeNo());
            paramMap.put("out_refund_no","tk"+paymentInfo.getOutTradeNo());
            paramMap.put("total_fee","1");
            paramMap.put("refund_fee","1");
            String paramXml = WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNER_KEY);
            //设置调用接口内容
            HttpClient client=new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            client.setXmlParam(paramXml);
            client.setHttps(true);
            //设置证书信息
            client.setCert(true);
            client.setCertPassword(ConstantPropertiesUtils.PARTNER);
            client.post();
            //接收返回数据
            String xml = client.getContent();
            System.out.println("xml"+xml);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            if (resultMap!=null && WXPayConstants.SUCCESS.equals(resultMap.get("result_code"))) {
                System.out.println("进来");
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackTime(new Date());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundService.updateById(refundInfo);
                return true;
            }
            System.out.println("1111111111111");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("2222222222222222");
            return false;
        }
    }

    //第三方接口封装
    private Map<String,String> getWXInterface(Map paramMap,String baseUrl,String utilsString) {
        try {
            //2、HTTPClient来根据URL访问第三方接口并且传递参数
            HttpClient client=new HttpClient(baseUrl);
            //client设置参数
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap,utilsString));
            client.setHttps(true); //支持https请求
            client.post();
            //3.返回第三方数据
            String xml = client.getContent();
            Map<String,String> resultMap=WXPayUtil.xmlToMap(xml);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
