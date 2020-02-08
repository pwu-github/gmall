package com.wp.gmall.service;

import com.wp.gmall.beans.PmsBaseAttrInfo;
import com.wp.gmall.beans.PmsBaseAttrValue;

import java.util.List;

public interface AttrService {
    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseAttrValue> getAttrValueList(String attrId);
}
