package com.mingri.yygh.hosp.service;

import com.mingri.yygh.model.hosp.Schedule;
import com.mingri.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface ScheduleService {
    //保存值班
    void save(Map<String, Object> map);

    //分页查询值班
    Page<Schedule> findPageSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo);

    //删除值班
    void remove(String hoscode, String hosScheduleId);

}
