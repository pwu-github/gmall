/**
 * FileName: CatalogService
 * Author: WP
 * Date: 2020/2/8 11:53
 * Description:
 * History:
 **/
package com.wp.gmall.service;

import com.wp.gmall.beans.PmsBaseCatalog1;
import com.wp.gmall.beans.PmsBaseCatalog2;
import com.wp.gmall.beans.PmsBaseCatalog3;

import java.util.List;

public interface CatalogService {

    public List<PmsBaseCatalog1> getCatalog1();

    List<PmsBaseCatalog2> getCatalog2(String catalog1Id);

    List<PmsBaseCatalog3> getCatalog3(String catalog2Id);
}
