/**
 * FileName: PmsSearchParam
 * Author: WP
 * Date: 2020/2/14 10:42
 * Description:
 * History:
 **/
package com.wp.gmall.beans;

import java.io.Serializable;
import java.util.List;

public class PmsSearchParam implements Serializable{
    private String catalog3Id;
    private String keyword;
    private List<PmsSkuAttrValue> pmsSkuAttrValues;

    public String getCatalog3Id() {
        return catalog3Id;
    }

    public void setCatalog3Id(String catalog3Id) {
        this.catalog3Id = catalog3Id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<PmsSkuAttrValue> getPmsSkuAttrValues() {
        return pmsSkuAttrValues;
    }

    public void setPmsSkuAttrValues(List<PmsSkuAttrValue> pmsSkuAttrValues) {
        this.pmsSkuAttrValues = pmsSkuAttrValues;
    }
}
