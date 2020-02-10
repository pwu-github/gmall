package com.wp.gmall.service;

import com.wp.gmall.beans.PmsBaseAttrInfo;
import com.wp.gmall.beans.PmsBaseAttrValue;
import com.wp.gmall.beans.PmsBaseSaleAttr;

import java.util.List;

public interface AttrService {
    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseAttrValue> getAttrValueList(String attrId);

    List<PmsBaseSaleAttr> baseSaleAttrList();

}
