package com.mingri.yygh.order.client;

import com.mingri.yygh.vo.order.OrderCountQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient("service-order")
@Repository
public interface OrderFeignClient {
    //远程调用获取订单统计数据
    @ApiOperation("获取订单统计数据")
    @PostMapping("/api/order/getCountMap")
    public Map<String,Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo);
}
