package com.mingri.yygh.hosp.service;

import com.mingri.yygh.model.hosp.Hospital;
import com.mingri.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface HospitalService {
    //保存
    void save(Map<String, Object> map);

    //根据医院编号查询医院信息
    Hospital getByHoscode(String hoscode);

    //分页查询医院
    Page<Hospital> getHospitalPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);
}
