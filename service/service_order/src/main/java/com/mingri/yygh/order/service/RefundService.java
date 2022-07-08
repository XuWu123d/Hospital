package com.mingri.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.yygh.model.order.PaymentInfo;
import com.mingri.yygh.model.order.RefundInfo;

public interface RefundService extends IService<RefundInfo> {
    //保存退款信息
    RefundInfo saveRefundInfo(PaymentInfo paymentInfo);

}
