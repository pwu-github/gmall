/**
 * FileName: PossportController
 * Author: WP
 * Date: 2020/2/19 14:39
 * Description:
 * History:
 **/
package com.wp.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.wp.gmall.beans.UmsMember;
import com.wp.gmall.service.UserService;
import com.wp.gmall.util.JwtUtil;
import com.wp.gmall.util.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@CrossOrigin
public class PassportController {

    @Reference
    private UserService userService;

    @RequestMapping("/index")
    public String index(String ReturnUrl, ModelMap modelMap) {

        modelMap.put("ReturnUrl", ReturnUrl);

        return "index";
    }

    @RequestMapping("/login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request) {
        String token = "";
        //调用用户服务验证用户名和密码
        UmsMember umsMemberLogin = userService.login(umsMember);
        if (umsMemberLogin != null) {
            //登陆成功

            //用jwt制作token
            String memberId = umsMemberLogin.getId();
            String nickname = umsMemberLogin.getNickname();
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("memberId", memberId);
            userMap.put("nickname", nickname);

            String ip = request.getHeader("x-forwarded-for"); //nginx转发的客户端IP
            if (StringUtils.isBlank(ip)) {
                ip = request.getRemoteAddr();
                if (StringUtils.isBlank(ip)) {
                    ip = "127.0.0.1";  //此处理论上要处理异常，不是写定
                }
            }
            //jwt加密token, "gmall"是服务器的秘钥, 第三个参数一般用MD5加密
//            String md5Ip = MD5Utils.md5(ip);
            token = JwtUtil.encode("com.wp.gmall", userMap, ip);
            //将token加入缓存
            userService.addUserToken(token, memberId);

        } else {
            //登陆失败，重新登陆
            token = "fail";
        }
        return token;
    }

    //该方法是在 拦截器中调用的
    @RequestMapping("/verify")
    @ResponseBody
    public String verify(String token, String currentIp) {

        //jwt校验token的真假
        Map<String, String> map = new HashMap<>();
//        String md5Ip = MD5Utils.md5(currentIp);
        Map<String, Object> decode = JwtUtil.decode(token, "com.wp.gmall", currentIp);
        if (decode != null) {

            map.put("status", "success");
            map.put("memberId", (String) decode.get("memberId"));
            map.put("nickname", (String) decode.get("nickname"));  // login()时放入Map中的
        } else {
            return "fail";
        }

        return JSON.toJSONString(map);
    }
}


