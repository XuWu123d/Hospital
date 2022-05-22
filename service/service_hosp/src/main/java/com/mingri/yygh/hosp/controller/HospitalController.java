package com.mingri.yygh.hosp.controller;

import com.mingri.yygh.common.result.Result;
import com.mingri.yygh.hosp.service.HospitalService;
import com.mingri.yygh.model.hosp.Hospital;
import com.mingri.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/hosp/hospital")
@CrossOrigin   //允许跨域
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    @GetMapping("list/{page}/{limit}")
    public Result getHospital(@PathVariable Integer page,
                              @PathVariable Integer limit,
                              HospitalQueryVo hospitalQueryVo) {
        Page<Hospital> hospitals=hospitalService.getHospitalPage(page,limit,hospitalQueryVo);
        return Result.ok(hospitals);
    }
}
