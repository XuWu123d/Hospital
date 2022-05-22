package com.mingri.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mingri.yygh.hosp.repository.DepartmentRepository;
import com.mingri.yygh.hosp.service.DepartmentService;
import com.mingri.yygh.model.hosp.Department;
import com.mingri.yygh.vo.hosp.DepartmentQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void save(Map<String, Object> map) {
        //将json转换为department对象
        String mapString= JSONObject.toJSONString(map);
        Department department = JSONObject.parseObject(mapString,Department.class);
        String hoscode = department.getHoscode();
        String depcode = department.getDepcode();
        Department depart = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (depart!=null) { //更新
            department.setId(depart.getId());
            department.setIsDeleted(0);
//            department.setCreateTime(depart.getCreateTime());
            department.setUpdateTime(new Date());
        } else {  //新增
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
        }
        departmentRepository.save(department);
    }

    //科室分页查询
    @Override
    public Page<Department> findPageDepartment(Integer page, Integer limit, DepartmentQueryVo departmentQueryVo) {
        Pageable pageable= PageRequest.of(page-1,limit);
        ExampleMatcher matcher= ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Department department=new Department();
        BeanUtils.copyProperties(departmentQueryVo,department);
        department.setIsDeleted(0);
        Example<Department> example = Example.of(department, matcher);

        Page<Department> all= departmentRepository.findAll(example,pageable);
        return all;
    }

    //删除科室
    @Override
    public void remove(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (department!=null) {
            departmentRepository.deleteById(department.getId());
        }
    }

}
