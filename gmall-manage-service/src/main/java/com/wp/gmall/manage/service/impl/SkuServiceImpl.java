/**
 * FileName: SkuServiceImpl
 * Author: WP
 * Date: 2020/2/10 19:15
 * Description:
 * History:
 **/
package com.wp.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.wp.gmall.beans.PmsSkuAttrValue;
import com.wp.gmall.beans.PmsSkuImage;
import com.wp.gmall.beans.PmsSkuInfo;
import com.wp.gmall.beans.PmsSkuSaleAttrValue;
import com.wp.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.wp.gmall.manage.mapper.PmsSkuImageMapper;
import com.wp.gmall.manage.mapper.PmsSkuInfoMapper;
import com.wp.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.wp.gmall.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    private PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    private PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    private PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Override
    public void savaSkuInfo(PmsSkuInfo pmsSkuInfo) {
        //保存skuInfo
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        //一定要在执行了保存操作后，才会主键返回策略，获得skuId
        String skuId = pmsSkuInfo.getId();
        //保存平台关联属性 base_attr
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }
        //保存销售属性
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }
        //保存图片
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }
    }
}
