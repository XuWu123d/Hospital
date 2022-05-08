package com.mingri.yygh.common.exception;

import com.mingri.yygh.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice  //拦截所有Controller类中出现的异常
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)  //指定什么类型的异常执行，Exception为所有异常的父类，因此一定会执行
    @ResponseBody  //转换为json格式进行输出（没有可以返回，但无法输出，
                   // 因为controller层中@RestController注解底层用了@ResponseBody注解，
                   // 所以controller的方法可以输出，而该类是一个普通类，如果要输出必须加@ResponseBody注解）
    public Result error(Exception e) {
        e.printStackTrace();
        return Result.fail();
    }
}
