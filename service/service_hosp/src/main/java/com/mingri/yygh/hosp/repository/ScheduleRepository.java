package com.mingri.yygh.hosp.repository;

import com.mingri.yygh.model.hosp.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.mongodb.repository.MongoRepository;

@Mapper
public interface ScheduleRepository extends MongoRepository<Schedule,String> {
    //根据hoscode和hosScheduleId获取schedule
    Schedule getScheduleByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);

}
