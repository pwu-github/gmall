/**
 * FileName: PossportController
 * Author: WP
 * Date: 2020/2/19 14:39
 * Description:
 * History:
 **/
package com.wp.gmall.passport.controller;

import com.wp.gmall.beans.UmsMember;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@CrossOrigin
public class PassportController {

    @RequestMapping("/index")
    public String index(String ReturnUrl, ModelMap modelMap){

        modelMap.put("ReturnUrl",ReturnUrl);

        return "index";
    }



    @RequestMapping("/login")
    @ResponseBody
    public String login(UmsMember umsMember){

        return "token";
    }

    @RequestMapping("/verify")
    @ResponseBody
    public String verify(String token){



        return "success";
    }
}


