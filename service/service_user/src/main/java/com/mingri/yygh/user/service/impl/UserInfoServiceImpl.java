package com.mingri.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.yygh.common.exception.HospitalException;
import com.mingri.yygh.common.helper.JwtHelper;
import com.mingri.yygh.common.result.Result;
import com.mingri.yygh.common.result.ResultCodeEnum;
import com.mingri.yygh.enums.AuthStatusEnum;
import com.mingri.yygh.model.user.Patient;
import com.mingri.yygh.model.user.UserInfo;
import com.mingri.yygh.user.mapper.UserInfoMapper;
import com.mingri.yygh.user.service.PatientService;
import com.mingri.yygh.user.service.UserInfoService;
import com.mingri.yygh.vo.user.LoginVo;
import com.mingri.yygh.vo.user.UserAuthVo;
import com.mingri.yygh.vo.user.UserInfoQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private PatientService patientService;

    //登录
    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        //获取手机号
        String phone = loginVo.getPhone();
        //获取验证码
        String code = loginVo.getCode();
        //判断手机号和验证码是否为空
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            throw new HospitalException(ResultCodeEnum.PARAM_ERROR);
        }
        //校验验证码是否正确
        String phoneCode = redisTemplate.opsForValue().get(phone);
        if (!code.equals(phoneCode)) {
            throw new HospitalException(ResultCodeEnum.CODE_ERROR);
        }

        //手机绑定
        UserInfo userInfo=null;
        if (!StringUtils.isEmpty(loginVo.getOpenid())) {
            userInfo = selectWxInfoOpenId(loginVo.getOpenid());
            if (userInfo!=null) {
                userInfo.setPhone(loginVo.getPhone());
                updateById(userInfo);
            } else {
                throw new HospitalException(ResultCodeEnum.DATA_ERROR);
            }
        }

        //如果userInfo为空，进行正常手机登录
        if (userInfo==null) {
            //查询数据库，判断手机号是否存在
            QueryWrapper<UserInfo> wrapper=new QueryWrapper<>();
            wrapper.eq("phone",phone);
            userInfo = userInfoMapper.selectOne(wrapper);
            //等于空，表明第一次登录，需要注册
            if (userInfo==null) {
                //将手机号添加进数据库
                UserInfo user=new UserInfo();
//            user.setName(phone);
                user.setStatus(1);
                user.setPhone(phone);
                userInfoMapper.insert(user);
            }
        }

        //校验是否被禁用
        if(userInfo.getStatus() == 0) {
            throw new HospitalException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }

        Map<String,Object> map=new HashMap<>();
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)) {
            name=userInfo.getNickName();
        }
        if (StringUtils.isEmpty(name)) {
            name= userInfo.getPhone();
        }
        //使用工具类获取token
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("name",name);
        map.put("token",token); //放在域中，也可以用session
        return map;
    }

    //查询数据库中扫码人信息
    @Override
    public UserInfo selectWxInfoOpenId(String openid) {
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("openid",openid);
        UserInfo userInfo = baseMapper.selectOne(queryWrapper);
        return userInfo;
    }

    //用户认证
    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //根据id查询用户信息
        UserInfo userInfo = userInfoMapper.selectById(userId);
        //认证人姓名
        userInfo.setName(userAuthVo.getName());
        //认证人其他信息
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        //进行信息更新
        userInfoMapper.updateById(userInfo);
    }

    //用户列表(条件查询带分页)
    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> userInfoPage, UserInfoQueryVo userInfoQueryVo) {
        String name = userInfoQueryVo.getKeyword(); //用户名称
        Integer status = userInfoQueryVo.getStatus();  //用户状态
        Integer authStatus = userInfoQueryVo.getAuthStatus(); //认证状态
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin();  //开始时间
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd();  //结束时间
        QueryWrapper<UserInfo> wrapper=new QueryWrapper<>();
        //判断这些条件是否不为空
        if (!StringUtils.isEmpty(name)) {
            wrapper.eq("name",name);
        }
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("status",status);
        }
        if (!StringUtils.isEmpty(authStatus)) {
            wrapper.eq("auth_status",authStatus);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time",createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time",createTimeEnd);
        }
        //查询
        IPage<UserInfo> pages = userInfoMapper.selectPage(userInfoPage, wrapper);
        //设置值(对信息进一步完善)
        for (UserInfo page:pages.getRecords()) {
            //处理认证状态编码
            page.getParam().put("authStatusString",AuthStatusEnum.getStatusNameByStatus(page.getAuthStatus()));
            //处理用户状态0 1
            String statusString = page.getStatus().intValue()==0 ? "锁定" : "正常";
            page.getParam().put("statusString",statusString);
        }
        return pages;
    }

    //用户锁定
    @Override
    public void lock(Long userId, Integer status) {
        if (status.intValue()==0 || status.intValue()==1) {
            UserInfo userInfo = userInfoMapper.selectById(userId);
            userInfo.setStatus(status);
            userInfoMapper.updateById(userInfo);
        }
    }

    //用户详情
    @Override
    public Map<String, Object> selectById(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        //处理认证状态编码
        userInfo.getParam().put("authStatusString",AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        //处理用户状态0 1
        String statusString=userInfo.getStatus().intValue()==0 ? "锁定" : "正常";
        userInfo.getParam().put("statusString",statusString);
        Map<String,Object> map=new HashMap<>();
        map.put("userInfo",userInfo);
        List<Patient> patientList = patientService.findAllById(userId);
        map.put("patientList",patientList);
        return map;
    }

    //用户认证
    @Override
    public void authList(Long userId, Integer authStatus) {
        if (authStatus.intValue()==2 || authStatus.intValue()==-1) { //2为通过，-1为不通过
            UserInfo userInfo = userInfoMapper.selectById(userId);
            userInfo.setStatus(authStatus);
            userInfoMapper.updateById(userInfo);
        }
    }

}
