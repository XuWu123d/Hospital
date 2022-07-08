package com.mingri.yygh.hosp.repository;

import com.mingri.yygh.model.hosp.Department;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface DepartmentRepository extends MongoRepository<Department,String> {
    //根据医院编号和科室编号查询科室信息
    Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode);

}
