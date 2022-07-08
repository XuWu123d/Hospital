package com.mingri.yygh.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mingri.yygh.model.order.PaymentInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付表mapper
 */
@Mapper
public interface PaymentMapper extends BaseMapper<PaymentInfo> {
}
