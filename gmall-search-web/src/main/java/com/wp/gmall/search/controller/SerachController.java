/**
 * FileName: SerachController
 * Author: WP
 * Date: 2020/2/14 9:28
 * Description:
 * History:
 **/
package com.wp.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.wp.gmall.beans.PmsBaseAttrInfo;
import com.wp.gmall.beans.PmsSearchParam;
import com.wp.gmall.beans.PmsSearchSkuInfo;
import com.wp.gmall.beans.PmsSkuAttrValue;
import com.wp.gmall.service.AttrService;
import com.wp.gmall.service.SearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@CrossOrigin
public class SerachController {

    @Reference
    private SearchService searchService;
    @Reference
    private AttrService attrService;

    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap) {
        //调用搜索服务，返回搜索结果
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.list(pmsSearchParam);
        modelMap.put("skuLsInfoList",pmsSearchSkuInfos);
        //抽取检索结果中包含的平台属性，因为有重复的，所以用set集合去重
        Set<String> valueIdSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }
        //根据valueId将属性列表查询出来
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrValueListByValueId(valueIdSet);
        modelMap.put("attrList",pmsBaseAttrInfos);
        return "list";
    }

    @RequestMapping("/index")
    public String index(){
        return "index";
    }
}
