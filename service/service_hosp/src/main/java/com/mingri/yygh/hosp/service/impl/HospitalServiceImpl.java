package com.mingri.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mingri.yygh.hosp.repository.HospitalRepository;
import com.mingri.yygh.hosp.service.HospitalService;
import com.mingri.yygh.model.hosp.Hospital;
import com.mingri.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class HospitalServiceImpl implements HospitalService {
    @Autowired
    private HospitalRepository hospitalRepository;

    @Override
    public void save(Map<String, Object> map) {
        //将前端传过来的json对象转换为string
        String str = JSONObject.toJSONString(map);
        //将字符串转换成对象
        Hospital hospital = JSONObject.parseObject(str,Hospital.class);
        //判断数据是否存在
        String hoscode = hospital.getHoscode();
        Hospital h = hospitalRepository.getHospitalByHoscode(hoscode);
        //存在则修改
        if (h!=null) {
            hospital.setStatus(h.getStatus());
            hospital.setCreateTime(h.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
        } else {  //不存在则添加
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
        }
        hospitalRepository.save(hospital);
    }

    @Override
    public Hospital getByHoscode(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        return hospital;
    }

    @Override
    public Page<Hospital> getHospitalPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        //创建pageable对象
        Pageable pageable = PageRequest.of(page - 1, limit);
        //构建条件匹配器
        ExampleMatcher matcher=ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Hospital hospital=new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);
        hospital.setIsDeleted(0);
        Example<Hospital> example=Example.of(hospital,matcher);
        Page<Hospital> all = hospitalRepository.findAll(example, pageable);
        return all;
    }

}
