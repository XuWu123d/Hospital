package com.mingri.yygh.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mingri.yygh.common.result.Result;
import com.mingri.yygh.common.utils.AuthContextHolder;
import com.mingri.yygh.enums.OrderStatusEnum;
import com.mingri.yygh.model.order.OrderInfo;
import com.mingri.yygh.order.service.OrderService;
import com.mingri.yygh.vo.order.OrderCountQueryVo;
import com.mingri.yygh.vo.order.OrderQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @ApiOperation("生成挂号订单")
    @PostMapping("saveOrder/{scheduleId}/{patientId}")
    public Result saveOrder(@PathVariable String scheduleId,@PathVariable Long patientId) {
        Long orderId = orderService.saveOrder(scheduleId,patientId);
        return Result.ok(orderId);
    }

    //订单列表(分页)
    @ApiOperation("订单列表(分页)")
    @GetMapping("list/{page}/{limit}")
    public Result list(@PathVariable Integer page, @PathVariable Integer limit,
                       OrderQueryVo orderQueryVo, HttpServletRequest request) {
        //设置当前用户id
        orderQueryVo.setUserId(AuthContextHolder.getUserId(request));
        Page<OrderInfo> orderPage=new Page<>(page,limit);
        IPage<OrderInfo> order = orderService.getPage(orderPage,orderQueryVo);
        return Result.ok(order);
    }

    //获取订单状态
    @ApiOperation("获取订单状态")
    @GetMapping("getStatusList")
    public Result getStatusList() {
        return Result.ok(OrderStatusEnum.getStatusList());
    }

    //订单详情显示
    @ApiOperation("订单详情显示")
    @GetMapping("orderList/{orderId}")
    public Result orderList(@PathVariable String orderId) {
        OrderInfo order = orderService.getOrder(orderId);
        return Result.ok(order);
    }

    //取消预约
    @ApiOperation("取消预约")
    @GetMapping("cancelBooking/{orderId}")
    public Result cancelBooking(@PathVariable Long orderId) {
        Boolean flag = orderService.cancelOrder(orderId);
        return Result.ok(flag);
    }

    @ApiOperation("获取订单统计数据")
    @PostMapping("getCountMap")
    public Map<String,Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo) {
        Map<String, Object> countMap = orderService.getCountMap(orderCountQueryVo);
        return countMap;
    }
}
