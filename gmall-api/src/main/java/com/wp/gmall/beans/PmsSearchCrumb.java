/**
 * FileName: PmsSearchCrumb
 * Author: WP
 * Date: 2020/2/17 16:18
 * Description:
 * History:
 **/
package com.wp.gmall.beans;

//面包屑
public class PmsSearchCrumb {

    private String valueId;
    private String valueName;
    private String urlParam;

    public String getValueId() {
        return valueId;
    }

    public void setValueId(String valueId) {
        this.valueId = valueId;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }

    public String getUrlParam() {
        return urlParam;
    }

    public void setUrlParam(String urlParam) {
        this.urlParam = urlParam;
    }
}
