package com.mingri.yygh.statistics.controller;

import com.mingri.yygh.common.result.Result;
import com.mingri.yygh.order.client.OrderFeignClient;
import com.mingri.yygh.vo.order.OrderCountQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    //获取订单统计数据
//    @ApiOperation("获取订单统计数据")
//    @GetMapping("getCountMap")
//    public Result getCountMap(OrderCountQueryVo orderCountQueryVo) {
//        Map<String, Object> countMap = orderFeignClient.getCountMap(orderCountQueryVo);
//        return Result.ok(countMap);
//    }


}
