package com.mingri.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.mingri.yygh.enums.PaymentTypeEnum;
import com.mingri.yygh.enums.RefundStatusEnum;
import com.mingri.yygh.model.order.PaymentInfo;
import com.mingri.yygh.model.order.RefundInfo;
import com.mingri.yygh.order.mapper.RefundMapper;
import com.mingri.yygh.order.service.PaymentService;
import com.mingri.yygh.order.service.RefundService;
import com.mingri.yygh.order.utils.ConstantPropertiesUtils;
import com.mingri.yygh.order.utils.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class RefundServiceImpl extends ServiceImpl<RefundMapper, RefundInfo> implements RefundService {
    @Autowired
    private PaymentService paymentService;

    //保存退款信息
    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {
        QueryWrapper<RefundInfo> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("order_id",paymentInfo.getOrderId());
        queryWrapper.eq("payment_type",paymentInfo.getPaymentType());
        RefundInfo refundInfo = baseMapper.selectOne(queryWrapper);
        //是否存在记录，存在直接返回
        if (refundInfo!=null) {
            return refundInfo;
        }
        //不存在，添加记录
        RefundInfo refund=new RefundInfo();
        refund.setCreateTime(new Date());
        refund.setOrderId(paymentInfo.getOrderId());
        refund.setPaymentType(paymentInfo.getPaymentType());
        refund.setOutTradeNo(paymentInfo.getOutTradeNo());
        refund.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());
        refund.setSubject(paymentInfo.getSubject());
        refund.setTotalAmount(paymentInfo.getTotalAmount());
        baseMapper.insert(refund);
        return refund;
    }

}
