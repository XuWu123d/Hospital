package com.mingri.yygh.hosp.controller;

import com.mingri.yygh.common.result.Result;
import com.mingri.yygh.hosp.service.ScheduleService;
import com.mingri.yygh.model.hosp.Schedule;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/hosp/schedule")
//@CrossOrigin
public class ScheduleController {
    @Autowired
    private ScheduleService scheduleService;

    //根据医院编号和科室编号获取排班数据
    @ApiOperation("根据医院编号和科室编号获取排班数据")
    @GetMapping("getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getScheduleList(@PathVariable Integer page,
                                    @PathVariable Integer limit,
                                    @PathVariable String hoscode,
                                    @PathVariable String depcode) {
        Map<String, Object> schedules= scheduleService.findPageScheduleByHoscodeAndDepcode(page,limit,hoscode,depcode);
        return Result.ok(schedules);
    }

    //根据医院编号，科室编号和工作日期，查询排班的详细信息
    @GetMapping("getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public Result getScheduleDetail(@PathVariable String hoscode,
                                    @PathVariable String depcode,
                                    @PathVariable String workDate) {
        List<Schedule> schedule=scheduleService.getScheduleDetail(hoscode,depcode,workDate);
        return Result.ok(schedule);
    }
}
