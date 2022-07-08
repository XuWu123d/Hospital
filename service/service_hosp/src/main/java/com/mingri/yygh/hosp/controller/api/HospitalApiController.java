package com.mingri.yygh.hosp.controller.api;

import com.mingri.yygh.common.result.Result;
import com.mingri.yygh.hosp.service.DepartmentService;
import com.mingri.yygh.hosp.service.HospitalService;
import com.mingri.yygh.hosp.service.HospitalSetService;
import com.mingri.yygh.hosp.service.ScheduleService;
import com.mingri.yygh.model.hosp.Hospital;
import com.mingri.yygh.model.hosp.Schedule;
import com.mingri.yygh.vo.hosp.DepartmentVo;
import com.mingri.yygh.vo.hosp.HospitalQueryVo;
import com.mingri.yygh.vo.hosp.ScheduleOrderVo;
import com.mingri.yygh.vo.order.SignInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp/hospital")
//@CrossOrigin
public class HospitalApiController {
    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation("查询医院列表")
    @GetMapping("findHospital/{page}/{limit}")
    public Result findHospital(@PathVariable Integer page,
                               @PathVariable Integer limit,
                               HospitalQueryVo hospitalQueryVo
                               ) {
        Page<Hospital> hospital = hospitalService.getHospitalPage(page, limit, hospitalQueryVo);
        return Result.ok(hospital);
    }

    @ApiOperation("根据医院名称查询")
    @GetMapping("findByHosname/{hosname}")
    public Result findByHosname(@PathVariable String hosname) {
        List<Hospital> hospitals=hospitalService.getByHosname(hosname);
        return Result.ok(hospitals);
    }

    @ApiOperation("根据名称查询医院信息")
    @GetMapping("findHospitalByName/{hoscode}")
    public Result findHospitalByName(@PathVariable String hoscode) {
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }

    @ApiOperation("根据医院编号查询所有科室")
    @GetMapping("findScheduleByHoscode/{hoscode}")
    public Result findScheduleByHoscode(@PathVariable String hoscode) {
        List<DepartmentVo> departments = departmentService.getDepartmentListByHoscode(hoscode);
        return Result.ok(departments);
    }

    @ApiOperation("根据医院编号获取预约挂号详情")
    @GetMapping("findHospitalDetail/{hoscode}")
    public Result findHospitalDetail(@PathVariable String hoscode) {
        Map<String,Object> result= hospitalService.getHospitalDetailByHoscode(hoscode);
        return Result.ok(result);
    }

    @ApiOperation("获取可预约排班数据")
    @GetMapping("getBookingSchedule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getBookingSchedule(@PathVariable Integer page,@PathVariable Integer limit,
                                     @PathVariable String hoscode,@PathVariable String depcode) {
        Map<String,Object> list=scheduleService.getBookingSchedule(page,limit,hoscode,depcode);
        return Result.ok(list);
    }

    @ApiOperation("获取排班数据")
    @GetMapping("findScheduleList/{hoscode}/{depcode}/{workDate}")
    public Result findScheduleList(@PathVariable String hoscode,
                                   @PathVariable String depcode,
                                   @PathVariable String workDate) {
        List<Schedule> scheduleDetail = scheduleService.getScheduleDetail(hoscode, depcode, workDate);
        return Result.ok(scheduleDetail);
    }

    @ApiOperation("根据排班id获取排班数据")
    @GetMapping("getScheduleById/{scheduleId}")
    public Result getScheduleById(@PathVariable String scheduleId) {
       Schedule schedule = scheduleService.getScheduleById(scheduleId);
        return Result.ok(schedule);
    }

    //远程调用接口(order模块调用)
    @ApiOperation("根据排班id获取预约下单数据")
    @GetMapping("inner/getSchedule/{scheduleId}")
    public ScheduleOrderVo getSchedule(@PathVariable String scheduleId) {
        ScheduleOrderVo schedule = scheduleService.getScheduleOrderVo(scheduleId);
        return schedule;
    }

    //远程调用获取签名信息，进行对比(order模块调用)
    @ApiOperation("获取医院签名信息")
    @GetMapping("getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(@PathVariable String hoscode) {
        SignInfoVo signInfoVo = hospitalSetService.getSignInfoVo(hoscode);
        return signInfoVo;
    }
}
