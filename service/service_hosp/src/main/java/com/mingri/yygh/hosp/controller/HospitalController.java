package com.mingri.yygh.hosp.controller;

import com.mingri.yygh.common.result.Result;
import com.mingri.yygh.hosp.service.HospitalService;
import com.mingri.yygh.model.hosp.Hospital;
import com.mingri.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("admin/hosp/hospital")
//@CrossOrigin   //允许跨域
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    //分页查询医院
    @GetMapping("list/{page}/{limit}")
    public Result getHospital(@PathVariable Integer page,
                              @PathVariable Integer limit,
                              HospitalQueryVo hospitalQueryVo) {
        Page<Hospital> hospitals=hospitalService.getHospitalPage(page,limit,hospitalQueryVo);
        return Result.ok(hospitals);
    }

    //更新医院上线状态
    @ApiOperation("更新医院上线状态")
    @GetMapping("updateHospStatus/{id}/{status}")
    public Result updateHospStatus(@PathVariable String id,
                                   @PathVariable Integer status) {
        hospitalService.updateStatus(id,status);
        return Result.ok();
    }

    //医院详情信息展示
    @ApiOperation("医院详情信息展示")
    @GetMapping("showHospitalDetail/{id}")
    public Result showHospitalDetail(@PathVariable String id) {
        Map<String, Object> hospitals = hospitalService.getHospitalDetailById(id);
        return Result.ok(hospitals);
    }

}
