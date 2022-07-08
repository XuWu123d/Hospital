package com.mingri.yygh.hosp.service;

import com.mingri.yygh.model.hosp.Schedule;
import com.mingri.yygh.vo.hosp.ScheduleOrderVo;
import com.mingri.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ScheduleService {
    //保存值班
    void save(Map<String, Object> map);

    //分页查询值班
    Page<Schedule> findPageSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo);

    //删除值班
    void remove(String hoscode, String hosScheduleId);

    //根据医院编号和科室编号获取排班数据
    Map<String, Object> findPageScheduleByHoscodeAndDepcode(Integer page, Integer limit, String hoscode, String depcode);

    //根据医院编号，科室编号和工作日期，查询排班的详细信息
    List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate);

    //获取可预约排班数据
    Map<String, Object> getBookingSchedule(Integer page, Integer limit, String hoscode, String depcode);

    //根据排班id获取排班数据
    Schedule getScheduleById(String scheduleId);

    //根据排班id获取预约下单数据
    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    //跟新排班数据，mq调用
    void update(Schedule schedule);
}
