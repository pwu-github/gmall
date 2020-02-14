package com.wp.gmall.service;

import com.wp.gmall.beans.PmsSearchParam;
import com.wp.gmall.beans.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {
    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam);
}
