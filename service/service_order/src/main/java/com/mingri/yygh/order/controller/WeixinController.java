package com.mingri.yygh.order.controller;

import com.mingri.yygh.common.result.Result;
import com.mingri.yygh.order.service.PaymentService;
import com.mingri.yygh.order.service.WeixinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/weixin")
public class WeixinController {

    @Autowired
    private WeixinService weixinService;

    @Autowired
    private PaymentService paymentService;

    //生成微信支付二维码
    @ApiOperation("生成微信支付二维码")
    @GetMapping("createNative/{orderId}")
    public Result createNative(@PathVariable Long orderId) {
        System.out.println("生成二维码");
        Map map=weixinService.createNative(orderId);
        return Result.ok(map);
    }

    //查询支付状态
    @ApiOperation("查询支付状态")
    @GetMapping("getPayStatus/{orderId}")
    public Result getPayStatus(@PathVariable Long orderId) {
        //调用微信接口查询支付状态
        Map<String,String> resultMap= weixinService.getPayStatus(orderId);
        System.out.println("支付状态resultMap："+resultMap);
        //判断状态是否为空
        if (resultMap==null) {
            return Result.fail().message("支付失败");
        }
        //判断是否支付成功
        if ("SUCCESS".equals(resultMap.get("trade_state"))) {
            //更新订单状况
            //订单编码
            String out_trade_no=resultMap.get("out_trade_no");
            //调用支付接口
            paymentService.paySuccess(out_trade_no,resultMap);
            return Result.ok().message("支付成功");
        }
        return Result.ok().message("支付中");
    }


}
