package com.mingri.yygh.hospital.client;

import com.mingri.yygh.vo.hosp.ScheduleOrderVo;
import com.mingri.yygh.vo.order.SignInfoVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("service-hosp")
@Repository
public interface HospitalFeignClient {
    //远程调用接口(order模块调用)
    @ApiOperation("根据排班id获取预约下单数据")
    @GetMapping("/api/hosp/hospital/inner/getSchedule/{scheduleId}")
    public ScheduleOrderVo getSchedule(@PathVariable("scheduleId") String scheduleId);

    //远程调用获取签名信息，进行对比(order模块调用)
    @ApiOperation("获取医院签名信息")
    @GetMapping("/api/hosp/hospital/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(@PathVariable("hoscode") String hoscode);
}
