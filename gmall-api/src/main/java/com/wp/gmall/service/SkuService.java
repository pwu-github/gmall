package com.wp.gmall.service;

import com.wp.gmall.beans.PmsSkuInfo;

public interface SkuService {
    void savaSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getSkuById(String skuId);
}
