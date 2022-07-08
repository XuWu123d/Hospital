package com.mingri.yygh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.yygh.model.user.Patient;

import java.util.List;

public interface PatientService extends IService<Patient> {
    //获取就诊人信息列表
    List<Patient> findAllById(Long userId);

    //根据id查询就诊人信息
    Patient getPatientById(Long id);
}
