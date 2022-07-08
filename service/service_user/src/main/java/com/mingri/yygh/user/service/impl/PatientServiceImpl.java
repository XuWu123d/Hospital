package com.mingri.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.yygh.cmn.client.CmnFeignClient;
import com.mingri.yygh.enums.DictEnum;
import com.mingri.yygh.model.user.Patient;
import com.mingri.yygh.user.mapper.PatientMapper;
import com.mingri.yygh.user.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper,Patient> implements PatientService {

    @Autowired
    private PatientMapper patientMapper;

    @Autowired
    private CmnFeignClient cmnFeignClient;

    //根据id查询所有就诊人信息
    @Override
    public List<Patient> findAllById(Long userId) {
        QueryWrapper<Patient> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        List<Patient> patients = patientMapper.selectList(queryWrapper);
        for (Patient patient : patients) {
            packPatient(patient);
        }
        return patients;
    }

    //根据id查询就诊人信息
    @Override
    public Patient getPatientById(Long id) {
        //根据id获取信息
        Patient patient = patientMapper.selectById(id);
        //对信息进一步完善
        packPatient(patient);
        return patient;
    }

    //封装，对信息进一步完善
    public void packPatient(Patient patient) {
        //根据证件类型编码，获取证件类型具体指
        String certificatesTypeString = cmnFeignClient.selectByDictcodeAndValue(DictEnum.CERTIFICATES_TYPE.getDictCode(), patient.getCertificatesType()); //联系人证件
        //联系人证件类型
        String contactsCertificatesTypeString =
                cmnFeignClient.selectByDictcodeAndValue(DictEnum.CERTIFICATES_TYPE.getDictCode(),patient.getContactsCertificatesType());
        //省
        String provinceString = cmnFeignClient.selectByValue(patient.getProvinceCode());
        //市
        String cityString = cmnFeignClient.selectByValue(patient.getCityCode());
        //区
        String districtString = cmnFeignClient.selectByValue(patient.getDistrictCode());
        patient.getParam().put("certificatesTypeString", certificatesTypeString);
        patient.getParam().put("contactsCertificatesTypeString", contactsCertificatesTypeString);
        patient.getParam().put("provinceString", provinceString);
        patient.getParam().put("cityString", cityString);
        patient.getParam().put("districtString", districtString);
        patient.getParam().put("fullAddress", provinceString + cityString + districtString + patient.getAddress());
    }
}
