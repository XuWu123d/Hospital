package com.mingri.yygh.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mingri.yygh.common.result.Result;
import com.mingri.yygh.model.user.UserInfo;
import com.mingri.yygh.user.service.UserInfoService;
import com.mingri.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 后台系统用户管理
 */
@RestController
@RequestMapping("/admin/user")
public class UserController {
    @Autowired
    private UserInfoService userInfoService;

    //用户列表(条件查询带分页)
    @ApiOperation("用户列表")
    @GetMapping("{page}/{limit}")
    public Result list(@PathVariable Long page, @PathVariable Long limit, UserInfoQueryVo userInfoQueryVo) {
        Page<UserInfo> userInfoPage=new Page<>(page,limit);
        IPage<UserInfo> list= userInfoService.selectPage(userInfoPage,userInfoQueryVo);
        return Result.ok(list);
    }

    //用户锁定
    @ApiOperation("用户锁定")
    @GetMapping("/lock/{userId}/{status}")
    public Result lock(@PathVariable Long userId,@PathVariable Integer status) {
        userInfoService.lock(userId,status);
        return Result.ok();
    }

    //用户详情
    @ApiOperation("用户详情")
    @GetMapping("show/{userId}")
    public Result show(@PathVariable Long userId) {
        Map<String,Object> map= userInfoService.selectById(userId);
        return Result.ok(map);
    }

    //用户认证
    @ApiOperation("用户认证")
    @GetMapping("authList/{userId}/{authStatus}")
    public Result authList(@PathVariable Long userId,@PathVariable Integer authStatus) {
        userInfoService.authList(userId,authStatus);
        return Result.ok();
    }
}
