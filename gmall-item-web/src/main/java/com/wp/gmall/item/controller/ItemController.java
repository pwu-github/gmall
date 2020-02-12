/**
 * FileName: ItemController
 * Author: WP
 * Date: 2020/2/11 10:46
 * Description:
 * History:
 **/
package com.wp.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.wp.gmall.beans.PmsProductSaleAttr;
import com.wp.gmall.beans.PmsSkuInfo;
import com.wp.gmall.beans.PmsSkuSaleAttrValue;
import com.wp.gmall.service.SkuService;
import com.wp.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
@CrossOrigin
public class ItemController {

    @Reference
    private SkuService skuService;
    @Reference
    private SpuService spuService;

    @RequestMapping("/index")
//    @ResponseBody注解会把 返回的“index”解析成json格式的数据，去掉这个注解，才可以把返回的 index 当做路径解析，访问index.html页面
    public String index(Model model){
        List<String> list = new ArrayList();
        for (int i = 0; i < 3; i++) {
            list.add("cycle data:"+i);
        }
        model.addAttribute("hello","hello world!");
        model.addAttribute("list",list);
        return "index";
    }

    //查询出sku详情，传递给页面
    @RequestMapping("/{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap modelMap){
        //sku详情
        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId);
        //"skuInfo" 是前端接受的参数，放入域中，前端取值
        modelMap.put("skuInfo",pmsSkuInfo);
        //销售属性
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrListCheckBySku(pmsSkuInfo.getProductId(),pmsSkuInfo.getId());
        modelMap.put("spuSaleAttrListCheckBySku",pmsProductSaleAttrs);
        
        //查询当前的sku所在的spu的其他sku集合的hash集合
        HashMap<String, String> skuSaleAttrHash = new HashMap<>();
        List<PmsSkuInfo> pmsSkuInfos = skuService.getSkuSaleAttrValueListBySpu(pmsSkuInfo.getProductId());

        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            String key = "";
            String value = skuInfo.getId();
            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                key += pmsSkuSaleAttrValue.getSaleAttrValueId()+"|";  //  123|122
            }
            //用查询出的sku的 sale_attr_value_id 作为 key，hash结构类似于  （123|122，111）
            skuSaleAttrHash.put(key,value);
        }
        //将hash解析成json字符串放入到页面
        String skuSaleAttrHashJsonStr = JSON.toJSONString(skuSaleAttrHash);
        modelMap.put("skuSaleAttrHashJsonStr",skuSaleAttrHashJsonStr);
        return "item";
    }

}
