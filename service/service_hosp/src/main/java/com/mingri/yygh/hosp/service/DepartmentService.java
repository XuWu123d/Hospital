package com.mingri.yygh.hosp.service;

import com.mingri.yygh.model.hosp.Department;
import com.mingri.yygh.vo.hosp.DepartmentQueryVo;
import com.mingri.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    //保存科室信息
    void save(Map<String, Object> map);

    //分页查询科室
    Page<Department> findPageDepartment(Integer page, Integer limit, DepartmentQueryVo departmentQueryVo);

    //删除科室
    void remove(String hoscode, String depcode);

    //根据医院编号查询所有科室信息
    List<DepartmentVo> getDepartmentListByHoscode(String hoscode);

    //根据科室编号，医院编号查询科室名称
    String getDepname(String hoscode, String depcode);

    //根据科室编号，医院编号查询科室信息
    Department getDepartment(String hoscode, String depcode);
}
