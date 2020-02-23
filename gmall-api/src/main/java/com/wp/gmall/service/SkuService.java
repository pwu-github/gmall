package com.wp.gmall.service;

import com.wp.gmall.beans.PmsSkuInfo;

import java.math.BigDecimal;
import java.util.List;

public interface SkuService {
    void savaSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getSkuById(String skuId);

    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);

    List<PmsSkuInfo> getAllSku();

    boolean checkPrice(String productSkuId, BigDecimal productPrice);
}
