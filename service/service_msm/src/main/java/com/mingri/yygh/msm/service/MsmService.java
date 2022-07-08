package com.mingri.yygh.msm.service;

import com.mingri.yygh.vo.msm.MsmVo;

public interface MsmService {
    //判断验证码是否
    boolean send(String phone, String code);

    //mq使用发送短信
    boolean send(MsmVo msmVo);
}
