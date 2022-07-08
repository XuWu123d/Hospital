package com.mingri.yygh.msm.controller;

import com.mingri.yygh.common.result.Result;
import com.mingri.yygh.msm.service.MsmService;
import com.mingri.yygh.msm.util.RandomUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/msm")
//@CrossOrigin
public class MsmController {

    @Autowired
    private MsmService msmService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    //短信发送
    @ApiOperation("短信发送")
    @GetMapping("/send/{phone}")
    public Result sendCode(@PathVariable String phone) {
        //从redis缓存中获取验证码
        String code = redisTemplate.opsForValue().get(phone);
        //没有就生成并获取，然后放入缓存中
        if (code==null) {
//            code = RandomUtil.getSixBitRandom();
            code="123456";
            boolean isSend= msmService.send(phone,code);
            if (isSend) {
                redisTemplate.opsForValue().set(phone,code,1, TimeUnit.MINUTES);
//                String s = redisTemplate.opsForValue().get(phone);
            } else {
                return Result.fail().message("短信发送失败");
            }
        }
        return Result.ok();
    }
}
