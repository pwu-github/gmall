/**
 * FileName: AttrServiceImpl
 * Author: WP
 * Date: 2020/2/8 16:59
 * Description:
 * History:
 **/
package com.wp.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.wp.gmall.beans.PmsBaseAttrInfo;
import com.wp.gmall.beans.PmsBaseAttrValue;
import com.wp.gmall.beans.PmsBaseSaleAttr;
import com.wp.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.wp.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.wp.gmall.manage.mapper.PmsBaseSaleAttrMapper;
import com.wp.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service  //引入dubbo包
public class AttrServiceImpl implements AttrService {

    @Autowired
    private PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;
    @Autowired
    private PmsBaseAttrValueMapper pmsBaseAttrValueMapper;
    @Autowired
    private PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;

    //根据三级分类查询属性信息
    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> baseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);
        return baseAttrInfos;
    }

    //保存 或 修改 属性信息
    @Override
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {
        String id = pmsBaseAttrInfo.getId();
        if(StringUtils.isBlank(id)){
            //保存属性
            // insert()将空字段值也插入  insertSelective（）只是插入不为空的字段值
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);
            //保存属性值,每个属性id 对应多个属性值，先获得每个属性对应的属性值集合
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            //每个属性值 对应的属性id 都是相同的
            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                //执行了insertSelective()操作后，pmsBaseAttrInfo.getId()是通过PmsBaseAttrInfo 中 @Id 和@GeneratedValue(strategy = GenerationType.IDENTITY)注解，主键返回策略
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                //保存属性值
                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            }
        }else {
            //修改属性
            //先更新属性
            Example example = new Example(PmsBaseAttrInfo.class);
            example.createCriteria().andEqualTo("id",pmsBaseAttrInfo.getId());
            pmsBaseAttrInfoMapper.updateByExample(pmsBaseAttrInfo,example);
            //修改属性值，先删除原来的属性值，再将新的属性值插入
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
            pmsBaseAttrValueMapper.delete(pmsBaseAttrValue);
            List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue baseAttrValue : pmsBaseAttrValues) {
                baseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValueMapper.insertSelective(baseAttrValue);
            }
        }

        return "success";
    }

    //根据属性id 获取属性值集合
    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        List<PmsBaseAttrValue> attrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
        return attrValues;
    }

    //获得基础销售属性
    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        return pmsBaseSaleAttrMapper.selectAll();
    }
}
