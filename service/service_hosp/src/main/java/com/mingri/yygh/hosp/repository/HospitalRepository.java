package com.mingri.yygh.hosp.repository;

import com.mingri.yygh.model.hosp.Hospital;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

@Mapper
public interface HospitalRepository extends MongoRepository<Hospital,String> {
    Hospital getHospitalByHoscode(String hoscode);

    //根据医院名称查询
    List<Hospital> getHospitalByHosnameLike(String hosname);
}
