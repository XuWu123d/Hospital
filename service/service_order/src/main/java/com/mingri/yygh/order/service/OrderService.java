package com.mingri.yygh.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.yygh.model.order.OrderInfo;
import com.mingri.yygh.vo.order.OrderCountQueryVo;
import com.mingri.yygh.vo.order.OrderQueryVo;

import java.util.Map;

public interface OrderService extends IService<OrderInfo> {
    //生成挂号订单
    Long saveOrder(String scheduleId, Long patientId);

    //订单详情显示
    OrderInfo getOrder(String orderId);

    //订单列表(分页)
    IPage<OrderInfo> getPage(Page<OrderInfo> orderPage, OrderQueryVo orderQueryVo);

    //取消预约
    Boolean cancelOrder(Long orderId);

    //就诊通知
    void patientTips();

    //预约统计
    Map<String,Object> getCountMap(OrderCountQueryVo orderCountQueryVo);
}
