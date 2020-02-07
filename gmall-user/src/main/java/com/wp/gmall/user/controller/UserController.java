/**
 * FileName: USerController
 * Author: WP
 * Date: 2020/2/6 11:07
 * Description:
 * History:
 **/
package com.wp.gmall.user.controller;

import com.wp.gmall.beans.UmsMember;
import com.wp.gmall.beans.UmsMemberReceiveAddress;
import com.wp.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/index")
    @ResponseBody
    public String index(){
        return "hello user!";
    }

    //获取全部用户信息
    @RequestMapping("/getAllUser")
    @ResponseBody
    public List<UmsMember> getAllUser(){
        List<UmsMember> umsMembers = userService.getAllUser();
        return umsMembers;
    }

    //根据用户id查询用户收货地址
    @RequestMapping("/getAddress")
    @ResponseBody
    public List<UmsMemberReceiveAddress> getAddressByUserId(@RequestParam String userId){
        List<UmsMemberReceiveAddress> addresses = userService.getAddressByUserId(userId);
        return addresses;
    }

}
