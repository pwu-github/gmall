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
import com.wp.gmall.util.HttpclientUtil;
import com.wp.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
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

    //新浪微博 第三方登录
    @RequestMapping("/vlogin")
    public String vlogin(String code, HttpServletRequest request) {
        //获取access_token
        String url = "https://api.weibo.com/oauth2/access_token?";
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "2443239074");
        map.put("client_secret", "bff971802e1c6fa5c7485aba36672727");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://passport.gmall.com:8085/vlogin");
        map.put("code", code);  //code可能会过期，需要重新生成
        //{"access_token":"2.00zvuRKEqFa2fCcf6f09b97cc9Sl6E","remind_in":"157679999","expires_in":157679999,"uid":"3816565123","isRealName":"true"}
        String access_token_json = HttpclientUtil.doPost(url, map);
        Map<String, Object> access_token_map = JSON.parseObject(access_token_json, Map.class);
        //获取用户信息
        String access_token = (String) access_token_map.get("access_token");
        String uid = (String) access_token_map.get("uid");
        String userInfoUrl = "https://api.weibo.com/2/users/show.json?access_token=" + access_token + "&uid=" + uid;
        String userInfoJson = HttpclientUtil.doGet(userInfoUrl);
        Map<String, Object> userInfoMap = JSON.parseObject(userInfoJson, Map.class);
        //将用户信息保存到数据库
        UmsMember umsMember = new UmsMember();
        umsMember.setSourceType("2");
        umsMember.setAccessCode(code);
        umsMember.setAccessToken(access_token);
        umsMember.setSourceUid((String) userInfoMap.get("idstr"));
        umsMember.setCity((String) userInfoMap.get("location"));
        umsMember.setNickname((String) userInfoMap.get("screen_name"));
        umsMember.setUsername((String) userInfoMap.get("screen_name"));
        umsMember.setCreateTime(new Date());
        umsMember.setJob("学生");
        umsMember.setGender("1");
        //保存之前，需要判断该用户信息是否已经存在
        UmsMember umsMember1 = new UmsMember();
        umsMember1.setSourceUid(umsMember.getSourceUid());
        UmsMember umsMember2 = userService.checkAutho2User(umsMember1);
        if (umsMember2 == null) {
            umsMember = userService.addAuthoUser(umsMember);
        } else {
            umsMember = umsMember2;
        }
        //生成jwt的token，重定向到首页时携带该token
        String token = "";
        String memberId = umsMember.getId();
        String nickname = umsMember.getNickname();
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

//        token = getToken(memberId, nickname, request);
        //将token加入缓存
        userService.addUserToken(token, memberId);

        //重定向到搜索首页
        return "redirect:http://search.gmall.com:8083/index?token="+token;
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

    //获得jwt的token
    public String getToken(String memberId, String nickname, HttpServletRequest request) {
        String token = "";
        //用jwt制作token
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
        return token;
    }
}


