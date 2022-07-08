package com.mingri.yygh.user.api;

import com.mingri.yygh.common.result.Result;
import com.mingri.yygh.common.utils.AuthContextHolder;
import com.mingri.yygh.model.user.UserInfo;
import com.mingri.yygh.user.service.UserInfoService;
import com.mingri.yygh.vo.user.LoginVo;
import com.mingri.yygh.vo.user.UserAuthVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
//@CrossOrigin
public class UserInfoApiController {
    @Autowired
    private UserInfoService userInfoService;

    //用手机号登录
    @ApiOperation("手机号登录")
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo) {
        Map<String,Object> user= userInfoService.login(loginVo);
        return Result.ok(user);
    }

    //用户认证接口
    @ApiOperation("用户认证接口")
    @PostMapping("userAuth")
    public Result userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request) {
        System.out.println(userAuthVo);
        Long userId = AuthContextHolder.getUserId(request);
        userInfoService.userAuth(userId,userAuthVo);
        return Result.ok();
    }

    //获取用户id信息接口
    @ApiOperation("获取用户id信息接口")
    @GetMapping("auth/getUserInfo")
    public Result getUserInfo(HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId(request);
        UserInfo userInfo = userInfoService.getById(userId);
        return Result.ok(userInfo);
    }
}
