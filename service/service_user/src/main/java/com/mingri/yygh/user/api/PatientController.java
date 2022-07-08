package com.mingri.yygh.user.api;

import com.mingri.yygh.common.result.Result;
import com.mingri.yygh.common.utils.AuthContextHolder;
import com.mingri.yygh.model.user.Patient;
import com.mingri.yygh.user.service.PatientService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 就诊人信息
 */
@RestController
@RequestMapping("/user/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    //获取就诊人列表
    @ApiOperation("获取就诊人列表")
    @GetMapping("getPatientList")
    public Result getPatientList(HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId(request);
        List<Patient> patients=patientService.findAllById(userId);
        return Result.ok(patients);
    }

    //添加就诊人信息
    @ApiOperation("添加就诊人信息")
    @PostMapping("savePatient")
    public Result savePatient(@RequestBody Patient patient,HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);
        patientService.save(patient);
        return Result.ok();
    }

    //根据id获取就诊人信息
    @ApiOperation("根据id获取就诊人信息")
    @GetMapping("getPatientById/{id}")
    public Result getPatientById(@PathVariable Long id) {
        //由于信息不够完善，需要在service层进一步处理
        Patient patient=patientService.getPatientById(id);
        return Result.ok(patient);
    }

    //修改就诊人
    @ApiOperation("修改就诊人")
    @PostMapping("updatePatient")
    public Result updatePatient(@RequestBody Patient patient) {
        patientService.updateById(patient);
        return Result.ok();
    }

    //删除就诊人信息
    @ApiOperation("删除就诊人信息")
    @DeleteMapping("deletePatient/{id}")
    public Result deletePatient(@PathVariable Long id) {
        patientService.removeById(id);
        return Result.ok();
    }

    //远程调用接口(order模块调用)
    @ApiOperation("根据id获取就诊人信息")
    @GetMapping("inner/getPatient/{id}")
    public Patient getPatient(@PathVariable Long id) {
        Patient patient = patientService.getPatientById(id);
        return patient;
    }
}
