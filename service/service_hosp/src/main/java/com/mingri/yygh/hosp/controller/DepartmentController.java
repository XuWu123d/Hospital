package com.mingri.yygh.hosp.controller;

import com.mingri.yygh.common.result.Result;
import com.mingri.yygh.hosp.service.DepartmentService;
import com.mingri.yygh.model.hosp.Department;
import com.mingri.yygh.vo.hosp.DepartmentVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 科室
 */
@RestController
@RequestMapping("/admin/hosp/department")
//@CrossOrigin
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    //查询科室
    @ApiOperation("查询医院所有科室列表")
    @GetMapping("/getDepartmentList/{hoscode}")
    public Result getDepartmentList(@PathVariable String hoscode) {
        List<DepartmentVo> departmentList= departmentService.getDepartmentListByHoscode(hoscode);
        return Result.ok(departmentList);
    }
}
