package com.mingri.yygh.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mingri.yygh.model.order.RefundInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退款表Mapper
 */
@Mapper
public interface RefundMapper extends BaseMapper<RefundInfo> {
}
