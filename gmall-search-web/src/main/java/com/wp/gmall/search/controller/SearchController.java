/**
 * FileName: SerachController
 * Author: WP
 * Date: 2020/2/14 9:28
 * Description:
 * History:
 **/
package com.wp.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.wp.gmall.annotations.LoginRequired;
import com.wp.gmall.beans.*;
import com.wp.gmall.service.AttrService;
import com.wp.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import tk.mybatis.mapper.util.StringUtil;

import java.util.*;

@Controller
@CrossOrigin
public class SearchController {

    @Reference
    private SearchService searchService;
    @Reference
    private AttrService attrService;

    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap) {
        //调用搜索服务，返回搜索结果
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.list(pmsSearchParam);
        modelMap.put("skuLsInfoList", pmsSearchSkuInfos);
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
        modelMap.put("attrList", pmsBaseAttrInfos);

        //已经选择过的属性id，需要删除，不能再选择。根据已经选择的属性过滤掉一部分数据
        String[] valueIds = pmsSearchParam.getValueId();
        if (valueIds != null) {
            //面包屑
            List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
            //将已经选择的属性和属性列表的属性进行比较，如果一致就删除查询出来的属性
            //删除时，不能直接循环删除，可能会出现IndexOutOfMemory异常。对于这种检查元素是否一致的删除情形，用迭代器是最合适的
            for (String valueId : valueIds) {
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
                //如果请求参数不为空，就说明当前请求中包含了属性参数，每一个属性参数对应了一个面包屑
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                pmsSearchCrumb.setValueId(valueId);
                pmsSearchCrumb.setUrlParam(getUrlParam(pmsSearchParam, valueId));
                //删除
                while (iterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        //获得属性值id
                        String attrValueId = pmsBaseAttrValue.getId();
                        if (valueId.equals(attrValueId)) {
                            //设置面包屑属性名称
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            iterator.remove();
                        }
                    }
                }
                pmsSearchCrumbs.add(pmsSearchCrumb);
            }
            modelMap.put("attrValueSelectedList", pmsSearchCrumbs);
        }
        String urlParam = getUrlParam(pmsSearchParam);
        modelMap.put("urlParam", urlParam);
        String keyword = pmsSearchParam.getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            modelMap.put("keyword", keyword);
        }
        return "list";
    }

    //获得搜索urlParam
    private String getUrlParam(PmsSearchParam pmsSearchParam) {
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] valueIds = pmsSearchParam.getValueId();
        String urlParam = "";

        if (StringUtils.isNotBlank(catalog3Id)) {
            //如果urlParam为空，说明还没有参数，那么catalog3Id就是第一个参数，则不需要加“&”，需要加“？”，前端已经加上了，这里不需要加。如果urlParam不为空，说明不是第一个参数，那就要加上“&”
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }
        if (valueIds != null) {
            for (String s : valueIds) {
                urlParam = urlParam + "&valueId=" + s;
            }
        }
        return urlParam;
    }

    //获得面包屑urlParam
    private String getUrlParam(PmsSearchParam pmsSearchParam, String... valueId) {
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] valueIds = pmsSearchParam.getValueId();
        String urlParam = "";

        if (StringUtils.isNotBlank(catalog3Id)) {
            //如果urlParam为空，说明还没有参数，那么catalog3Id就是第一个参数，则不需要加“&”，需要加“？”，前端已经加上了，这里不需要加。如果urlParam不为空，说明不是第一个参数，那就要加上“&”
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }
        if (valueIds != null) {
            for (String s : valueIds) {
                if (!s.equals(valueId)) {
                    urlParam = urlParam + "&valueId=" + s;
                }
            }
        }
        return urlParam;
    }

    @RequestMapping("/index")
    @LoginRequired(loginSuccess = false)
    public String index() {
        return "index";
    }
}
