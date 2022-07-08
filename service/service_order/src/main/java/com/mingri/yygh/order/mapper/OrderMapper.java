package com.mingri.yygh.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mingri.yygh.model.order.OrderInfo;
import com.mingri.yygh.vo.order.OrderCountQueryVo;
import com.mingri.yygh.vo.order.OrderCountVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单表mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderInfo> {
    //查询预约统计数据的方法                    将OrderCountQueryVo实体类起别名为vo
    List<OrderCountVo> selectOrderCount(@Param("vo") OrderCountQueryVo orderCountQueryVo);
}
