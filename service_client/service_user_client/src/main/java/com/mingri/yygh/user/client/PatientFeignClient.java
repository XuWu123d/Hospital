package com.mingri.yygh.user.client;

import com.mingri.yygh.model.user.Patient;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("service-user")
@Repository
public interface PatientFeignClient {
    //远程调用接口(order模块调用)
    @ApiOperation("根据id获取就诊人信息")
    @GetMapping("/user/patient/inner/getPatient/{id}")
    public Patient getPatient(@PathVariable("id") Long id);
}

