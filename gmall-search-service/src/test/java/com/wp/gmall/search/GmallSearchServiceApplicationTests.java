package com.wp.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.wp.gmall.beans.PmsSearchSkuInfo;
import com.wp.gmall.beans.PmsSkuInfo;
import com.wp.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GmallSearchServiceApplicationTests {

	@Reference
	private SkuService skuService;
	@Autowired
	private JestClient jestClient;

	@Test
	public void test() throws Exception {


	}

	public void put() throws Exception {
        //查询MySQL数据
        List<PmsSkuInfo> pmsSkuInfoList = skuService.getAllSku();

        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(pmsSearchSkuInfo,pmsSkuInfo);
            pmsSearchSkuInfos.add(pmsSearchSkuInfo);
        }

        //导入es
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            Index index = new Index.Builder(pmsSearchSkuInfo).index("gmall").type("PmsSkuInfo").id(pmsSearchSkuInfo.getId()).build();
            jestClient.execute(index);
        }
    }

}
