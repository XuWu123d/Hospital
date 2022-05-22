package com.mingri.yygh.hosp.controller.api;

import com.mingri.yygh.common.result.Result;
import com.mingri.yygh.hosp.service.HospitalService;
import com.mingri.yygh.model.hosp.Hospital;
import com.mingri.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("admin/hosp/hospital")
@CrossOrigin   //允许跨域
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    public Result getHospital(@PathVariable Integer page,
                              @PathVariable Integer limit,
                              HospitalQueryVo hospitalQueryVo) {
        Page<Hospital> hospitals=hospitalService.getHospitalPage(page,limit,hospitalQueryVo);
        return Result.ok(hospitals);
    }
}
