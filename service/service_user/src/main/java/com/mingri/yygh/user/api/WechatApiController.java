package com.mingri.yygh.user.api;

import com.alibaba.fastjson.JSONObject;
import com.mingri.yygh.common.helper.JwtHelper;
import com.mingri.yygh.common.result.Result;
import com.mingri.yygh.model.user.UserInfo;
import com.mingri.yygh.user.service.UserInfoService;
import com.mingri.yygh.user.utils.ConstantWechatUtils;
import com.mingri.yygh.user.utils.HttpClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信登录
 */
@Controller
@RequestMapping("/api/ucenter/wx")
public class WechatApiController {

    @Autowired
    private UserInfoService userInfoService;

    //回调
    @GetMapping("callback")
    public String callback(String code,String state) {
        //使用code和微信id和秘钥，请求微信固定地址，得到两个值
        StringBuffer buffer=new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        //设置值
        String accessTokenUrl = String.format(buffer.toString(),
                ConstantWechatUtils.WX_OPEN_APP_ID,
                ConstantWechatUtils.WX_OPEN_APP_SECRET,
                code);
        //使用httpclient请求这个地址
        try {
            String accessTokenInfo = HttpClientUtils.get(accessTokenUrl);
            JSONObject jsonObject = JSONObject.parseObject(accessTokenInfo);
            //获取access_token和openid
            String access_token = jsonObject.getString("access_token");
            String openid = jsonObject.getString("openid");
            //判断数据库是否存在扫码人信息
            UserInfo userInfo=userInfoService.selectWxInfoOpenId(openid);
            if (userInfo==null) { //不存在
                //拿着openid和access_token请求微信地址，得到扫码人信息
                String baseUrl="https://api.weixin.qq.com/sns/userinfo"+"?access_token=%s"+"&openid=%s";
                String baseInfoUrl = String.format(baseUrl, access_token, openid);
                String resultInfo = HttpClientUtils.get(baseInfoUrl);

                JSONObject resultUserInfoJson = JSONObject.parseObject(resultInfo);
                //获取用户昵称和头像
                String nikename = resultUserInfoJson.getString("nikename");
                String headimgurl = resultUserInfoJson.getString("headimgurl");
                //设置进数据库中
                userInfo=new UserInfo();
                userInfo.setNickName(nikename);
                userInfo.setOpenid(openid);
                userInfo.setStatus(1);
                userInfoService.save(userInfo);
            }
            //返回name和和token字符串
            Map<String,Object> map=new HashMap<>();
            String name = userInfo.getName();
            if (StringUtils.isEmpty(name)) {
                name=userInfo.getNickName();
            }
            if (StringUtils.isEmpty(name)) {
                name=userInfo.getPhone();
            }
            map.put("name",name);
            //判断userInfo是否有手机号，如果手机号为空，返回openid
            //如果手机号不为空，返回openid值是空字符串
            //前端判断：如果openid不为空，绑定手机号，如果openid为空，不需要绑定手机号
            if (StringUtils.isEmpty(userInfo.getPhone())) {
                map.put("openid",userInfo.getOpenid());
            } else {
                map.put("openid","");
            }
            //用jwt生成token字符串
            String token = JwtHelper.createToken(userInfo.getId(), name);
            map.put("token",token);
            //跳转到前端页面pages/weixin/callback.vue
            return "redirect:" + ConstantWechatUtils.YYGH_BASE_URL + "/weixin/callback?token="+map.get("token")+ "&openid="+map.get("openid")+"&name="+URLEncoder.encode((String) map.get("name"),"utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    //微信扫码登录
    @ResponseBody
    @GetMapping("/getLogin")
    public Result getConnect() {
        try {
            Map<String,Object> map=new HashMap<>();
            map.put("appid", ConstantWechatUtils.WX_OPEN_APP_ID);
            map.put("scope","snsapi_login");
            String redirectUrl = URLEncoder.encode(ConstantWechatUtils.WX_OPEN_REDIRECT_URL,"utf-8");
            map.put("redirect_uri",redirectUrl);
            map.put("state",System.currentTimeMillis()+"");
            return Result.ok(map);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}

