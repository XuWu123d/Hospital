package com.mingri.yygh.hosp.repository;

import com.mingri.yygh.model.hosp.Department;
import com.mingri.yygh.model.hosp.Hospital;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface HospitalRepository extends MongoRepository<Hospital,String> {
    Hospital getHospitalByHoscode(String hoscode);

}
