package com.wp.gmall.service;

import com.wp.gmall.beans.OmsCartItem;

import java.util.List;

public interface CartService {
    OmsCartItem cartExitByUser(String memberId, String skuId);

    void addCart(OmsCartItem omsCartItemFromDb);

    void updateCart(OmsCartItem omsCartItemFromDb);

    void flushCartCache(String memberId);

    List<OmsCartItem> cartList(String memberId);

    void checkCart(OmsCartItem omsCartItem);

    void delCartskuById(String productSkuId);
}
