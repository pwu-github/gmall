/**
 * FileName: PaymentServiceMqListener
 * Author: WP
 * Date: 2020/2/27 10:36
 * Description:
 * History:
 **/
package com.wp.gmall.payment.mq;

import com.wp.gmall.beans.PaymentInfo;
import com.wp.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

@Component
public class PaymentServiceMqListener {

    @Autowired
    private PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_CHECK_QUEUE", containerFactory = "jmsQueueListener")
    public void consumerPaymentCheckResult(MapMessage mapMessage) throws JMSException {
        String out_trade_no = mapMessage.getString("out_trade_no");
        //支付检查
        Map<String,Object> resultMap = paymentService.checkAlipayPayment(out_trade_no);
        if(resultMap != null && !resultMap.isEmpty()){
            String trade_status = (String) resultMap.get("trade_status");

            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setAlipayTradeNo((String)resultMap.get("trade_no"));
            paymentInfo.setCallbackContent((String) resultMap.get("call_back_content"));
            paymentInfo.setCallbackTime(new Date());
            paymentService.updatePayment(paymentInfo);
            return;
        }


    }
}
