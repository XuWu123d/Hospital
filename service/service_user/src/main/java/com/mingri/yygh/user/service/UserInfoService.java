package com.mingri.yygh.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mingri.yygh.model.user.UserInfo;
import com.mingri.yygh.vo.user.LoginVo;
import com.mingri.yygh.vo.user.UserAuthVo;
import com.mingri.yygh.vo.user.UserInfoQueryVo;

import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {
    //用户登录
    Map<String, Object> login(LoginVo loginVo);

    //查询数据库中扫码人信息
    UserInfo selectWxInfoOpenId(String openid);

    //用户认证
    void userAuth(Long userId, UserAuthVo userAuthVo);

    //用户列表(条件查询带分页)
    IPage<UserInfo> selectPage(Page<UserInfo> userInfoPage, UserInfoQueryVo userInfoQueryVo);

    //用户锁定
    void lock(Long userId, Integer status);

    //用户详情
    Map<String, Object> selectById(Long userId);

    //用户认证
    void authList(Long userId, Integer authStatus);
}
