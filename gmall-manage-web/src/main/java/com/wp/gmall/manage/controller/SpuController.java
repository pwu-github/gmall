/**
 * FileName: SpuController
 * Author: WP
 * Date: 2020/2/9 11:00
 * Description:
 * History:
 **/
package com.wp.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.wp.gmall.beans.PmsProductInfo;
import com.wp.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@CrossOrigin
public class SpuController {

    @Reference
    private SpuService spuService;


    @RequestMapping("/spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(String catalog3Id){
        List<PmsProductInfo> pmsProductInfos = spuService.spuList(catalog3Id);
        return pmsProductInfos;
    }

    @RequestMapping("/saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){

        return "success";
    }

    @RequestMapping("/fileUpdate")
    @ResponseBody
    public String fileUpdate(@RequestParam("file") MultipartFile multipartFile){

        return "success";
    }
}
