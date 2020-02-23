package com.wp.gmall.service;

import com.wp.gmall.beans.OmsOrder;

public interface OrderService {
    String checkTradeCode(String memberId,String tradeCode);

    String genTradeCode(String memberId);

    void saveOrder(OmsOrder omsOrder);
}
