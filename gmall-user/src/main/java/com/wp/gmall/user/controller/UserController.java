/**
 * FileName: USerController
 * Author: WP
 * Date: 2020/2/6 11:07
 * Description:
 * History:
 **/
package com.wp.gmall.user.controller;

import com.wp.gmall.user.bean.UmsMember;
import com.wp.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @RequestMapping("/getAllUser")
    @ResponseBody
    public List<UmsMember> getAllUser(){
        List<UmsMember> umsMembers = userService.getAllUser();
        return umsMembers;
    }

}
