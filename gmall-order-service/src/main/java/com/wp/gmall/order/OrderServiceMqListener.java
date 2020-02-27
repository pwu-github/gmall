/**
 * FileName: OrderServiceMqListener
 * Author: WP
 * Date: 2020/2/26 15:52
 * Description:
 * History:
 **/
package com.wp.gmall.order;

import com.wp.gmall.beans.OmsOrder;
import com.wp.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Session;

@Component
public class OrderServiceMqListener {

    @Autowired
    private OrderService orderService;

    @JmsListener(destination = "PAYMENT_SUCCESS_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(MapMessage mapMessage) throws JMSException {
        String out_trade_no = mapMessage.getString("out_trade_no");
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(out_trade_no);
        orderService.updateOrder(omsOrder);


    }
}
