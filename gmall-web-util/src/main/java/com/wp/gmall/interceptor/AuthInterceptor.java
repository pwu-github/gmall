package com.wp.gmall.interceptor;

import com.alibaba.fastjson.JSON;
import com.wp.gmall.annotations.LoginRequired;
import com.wp.gmall.util.CookieUtil;
import com.wp.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.HttpClientUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //反射获取被拦截请求的注解
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequired methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequired.class);
        //如果没有 拦截请求的注解，说明不用拦截，直接放行
        if (methodAnnotation == null) {
            return true;
        }
        String token = "";
        //cookie中的token
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
            /*如果 oldToken和newToken都为空，说明从未登陆过。
            如果 oldToken和newToken都不为空，说明oldToken过期了，newToken赋值给token
            如果 oldToken为空，newToken不为空，说明刚刚登陆。
            如果 oldToken不为空和newToken为空，说明之前登陆过，oldToken赋值发给token。
             */
        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }
        //浏览器携带的token
        String newToken = request.getParameter("token");
        //如果newToken不为空
        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }
        //有拦截的注解，需要拦截，进一步判断拦截的请求是否必须要登陆成功
        boolean loginSuccess = methodAnnotation.loginSuccess();
        //认证中心认证
        String success = "fail";
        Map<String, String> successMap = new HashMap<>();
        if (StringUtils.isNotBlank(token)) {
            String ip = request.getHeader("x-forwarded-for"); //nginx转发的客户端IP
            if (StringUtils.isBlank(ip)) {
                ip = request.getRemoteAddr();
                if (StringUtils.isBlank(ip)) {
                    ip = "127.0.0.1";  //此处理论上要处理异常，不是写定
                }
            }
            String successJson = HttpclientUtil.doGet("http://localhost:8085/verify?token=" + token+"&currentIp="+ip);
            successMap = JSON.parseObject(successJson, Map.class);
            success = successMap.get("status");
        }
        if (loginSuccess) {
            //需要登陆成功才可以进行下一步操作
            if (!success.equals("success")) {
                //登陆失败，重定向回到认证中心
                StringBuffer requestURL = request.getRequestURL();
                response.sendRedirect("http://localhost:8085/index?ReturnUrl=" + requestURL);
                return false;
            }
            //登陆成功，覆盖cookie中的token
            request.setAttribute("memberId", successMap.get("memberId"));
            request.setAttribute("nickname", successMap.get("nickname"));
            if (StringUtils.isNotBlank(token)) {
                //验证通过，覆盖cookie中的token值
                CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 2, true);
            }
        } else {
            //不需要必须登录成功，就可以进入下一步操作
            if (success.equals("success")) {
                request.setAttribute("memberId", successMap.get("memberId"));
                request.setAttribute("nickname", successMap.get("nickname"));
                if (StringUtils.isNotBlank(token)) {
                    //验证通过，覆盖cookie中的token值
                    CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 2, true);
                }
            }
        }
        return true;
    }
}
