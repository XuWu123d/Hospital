package com.mingri.yygh.hosp.repository;

import com.mingri.yygh.model.hosp.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

@Mapper
public interface ScheduleRepository extends MongoRepository<Schedule,String> {
    //根据hoscode和hosScheduleId获取schedule
    Schedule getScheduleByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);

    //根据医院编号，科室编号和工作日期，查询排班的详细信息
    List<Schedule> getScheduleByHoscodeAndDepcodeAndWorkDate(String hoscode, String depcode, Date workDate);
}
