package com.mingri.yygh.hosp.service;

import com.mingri.yygh.model.hosp.Hospital;
import com.mingri.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface HospitalService {
    //保存
    void save(Map<String, Object> map);

    //根据医院编号查询医院信息
    Hospital getByHoscode(String hoscode);

    //分页查询医院
    Page<Hospital> getHospitalPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

    //修改医院状态（上线/下线）
    void updateStatus(String id, Integer status);

    //根据id查询医院预约挂号详情信息
    Map<String,Object> getHospitalDetailById(String id);

    //根据医院编号获取医院名称
    String getHospName(String hoscode);

    //根据医院名称查询
    List<Hospital> getByHosname(String hosname);

    //根据医院编号获取预约挂号详情
    Map<String, Object> getHospitalDetailByHoscode(String hoscode);
}
