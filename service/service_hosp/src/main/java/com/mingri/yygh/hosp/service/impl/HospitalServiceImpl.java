package com.mingri.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mingri.yygh.cmn.client.CmnFeignClient;
import com.mingri.yygh.hosp.repository.HospitalRepository;
import com.mingri.yygh.hosp.service.HospitalService;
import com.mingri.yygh.model.hosp.Hospital;
import com.mingri.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class HospitalServiceImpl implements HospitalService {
    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private CmnFeignClient cmnFeignClient;

    //保存
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

    //根据医院编号查询医院信息
    @Override
    public Hospital getByHoscode(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        return hospital;
    }

    //分页查询医院
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
        all.getContent().stream().forEach(item ->{
            this.setHospitalHosType(item);
        });

//        cmnFeignClient.s
        return all;
    }

    private void setHospitalHosType(Hospital hospital) {
        String provinceCode = cmnFeignClient.selectByValue(hospital.getProvinceCode());
        String cityCode = cmnFeignClient.selectByValue(hospital.getCityCode());
        String districtCode = cmnFeignClient.selectByValue(hospital.getDistrictCode());
        String hostype = cmnFeignClient.selectByDictcodeAndValue("Hostype", hospital.getHostype());
        hospital.getParam().put("fullAddress",provinceCode+cityCode+districtCode);
        hospital.getParam().put("hostype",hostype);
    }

}
