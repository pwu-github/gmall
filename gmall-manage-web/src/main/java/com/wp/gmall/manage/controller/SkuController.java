/**
 * FileName: SkuController
 * Author: WP
 * Date: 2020/2/10 18:55
 * Description:
 * History:
 **/
package com.wp.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.wp.gmall.beans.PmsSkuInfo;
import com.wp.gmall.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@CrossOrigin
public class SkuController {

    @Reference
    private SkuService skuService;

    @RequestMapping("/saveSkuInfo")
    @ResponseBody
    public String savaSkuInfo(@RequestBody PmsSkuInfo pmsSkuInfo){

        //前端封装的是 skuId,PmsSkuInfo中的属性是 productId（数据库字段），于是在PmsSkuInfo中增加一个属性 skuId，再将skuId设置给productId
        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());
        //处理默认图片,如果前台没有选择默认图片，就把第一张上传的图片设置为默认
        String skuDefaultImg = pmsSkuInfo.getSkuDefaultImg();
        if(StringUtils.isBlank(skuDefaultImg)){
            pmsSkuInfo.setSkuDefaultImg(pmsSkuInfo.getSkuImageList().get(0).getImgUrl());
        }
        skuService.savaSkuInfo(pmsSkuInfo);
        return "success";
    }
}
