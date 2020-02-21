/**
 * FileName: TestOauth2
 * Author: WP
 * Date: 2020/2/20 21:05
 * Description:
 * History:
 **/
package com.wp.gmall.passport.controller;


import com.alibaba.fastjson.JSON;
import com.wp.gmall.util.HttpclientUtil;

import java.util.HashMap;
import java.util.Map;

public class TestOauth2 {
    static String client_id = "2443239074";
    static String redirect_uri = "http://passport.gmall.com:8085/vlogin";
    static String client_secret = "bff971802e1c6fa5c7485aba36672727";

    public static void main(String[] args) {

    }


    public static String getCode(){
        //"http://passport.gmall.com:8085/vlogin?code=d9ea2b296d5fe3f8597ce01e567ebdc9"
        String codeUrl = HttpclientUtil.doGet("https://api.weibo.com/oauth2/authorize?" + "client_id=" + client_id + "&redirect_uri=" + redirect_uri);
        //需要打开浏览器授权，获得code
        return codeUrl;
    }

    public static Map<String,String> getAccessToken(){
        String url = "https://api.weibo.com/oauth2/access_token?";
        Map<String, String> map = new HashMap<>();
        map.put("client_id",client_id);
        map.put("client_secret",client_secret);
        map.put("grant_type","authorization_code");
        map.put("redirect_uri",redirect_uri);
        map.put("code","d9ea2b296d5fe3f8597ce01e567ebdc9");
        String access_token_json = HttpclientUtil.doPost(url, map);
        Map<String,String> map1 = JSON.parseObject(access_token_json, Map.class);
        System.out.println(map1.get("access_token"));
        System.out.println(map1.get("uid"));
        return map1;
    }

    public static Map<String, String> getUserInfo(){
        Map<String, String> map = getAccessToken();
        String access_token = map.get("access_token");
        String uid = map.get("uid");
        String userInfoUrl = "https://api.weibo.com/2/users/show.json?access_token="+access_token+"&uid="+uid;
        String s = HttpclientUtil.doGet(userInfoUrl);
        Map<String, String> userInfoMap = JSON.parseObject(s, Map.class);

        return userInfoMap;
    }
}
