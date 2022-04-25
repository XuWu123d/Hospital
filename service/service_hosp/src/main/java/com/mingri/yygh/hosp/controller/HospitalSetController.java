package com.mingri.yygh.hosp.controller;

import com.mingri.yygh.hosp.service.HospitalSetService;
import com.mingri.yygh.model.hosp.HospitalSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "医院设置管理")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation(value = "获取所有医院设置")
    @GetMapping("findAll")
    public List<HospitalSet> select() {
        List<HospitalSet> list= hospitalSetService.list();
        return list;
    }

    @ApiOperation(value = "逻辑删除医院设置")
    @DeleteMapping("{id}")
    public boolean delete(@PathVariable Long id) {
        boolean flag = hospitalSetService.removeById(id);
        return flag;
    }
}
