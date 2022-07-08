package com.mingri.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mingri.yygh.hosp.repository.DepartmentRepository;
import com.mingri.yygh.hosp.service.DepartmentService;
import com.mingri.yygh.model.hosp.Department;
import com.mingri.yygh.vo.hosp.DepartmentQueryVo;
import com.mingri.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    //根据医院编号查询所有科室信息
    @Override
    public List<DepartmentVo> getDepartmentListByHoscode(String hoscode) {
        //封装结果
        List<DepartmentVo> departmentList=new ArrayList<>();
        //根据医院编号查询所有科室列表
        Department department=new Department();
        department.setHoscode(hoscode);
        Example<Department> example=Example.of(department);
        List<Department> departmentAll = departmentRepository.findAll(example);
        //对科室进行分类
        Map<String, List<Department>> departmentMap = departmentAll.stream().collect(Collectors.groupingBy(Department::getBigcode));
        for (String bigCode:departmentMap.keySet()) {
            List<Department> departments = departmentMap.get(bigCode);
            //封装大科室
            DepartmentVo bigDepartment=new DepartmentVo();
            //封装大科室编号
            bigDepartment.setDepcode(bigCode);
            //封装大科室名称(子科室共同拥有大科室名称，任意一个子科室都有)
            bigDepartment.setDepname(departments.get(0).getBigname());
            //封装小科室
            List<DepartmentVo> child=new ArrayList<>();
            //对小科室进行遍历
            for (Department childDepartment : departments) {
                //获取小科室编号
                String depcode = childDepartment.getDepcode();
                //获取小科室名称
                String depname = childDepartment.getDepname();
                DepartmentVo departmentVo=new DepartmentVo();
                departmentVo.setDepcode(depcode);
                departmentVo.setDepname(depname);
                //把小科室封装到list集合中
                child.add(departmentVo);
            }
            //将小科室放进大科室
            bigDepartment.setChildren(child);
            //放入最终结果集中
            departmentList.add(bigDepartment);
        }
        return departmentList;
    }

    //根据科室编号，医院编号查询科室名称
    @Override
    public String getDepname(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (department!=null) {
            return department.getDepname();
        }
        return null;
    }

    //根据科室编号，医院编号查询科室名称
    @Override
    public Department getDepartment(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        return department;
    }

}
