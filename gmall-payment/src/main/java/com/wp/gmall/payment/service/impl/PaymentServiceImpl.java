/**
 * FileName: PaymentServiceImpl
 * Author: WP
 * Date: 2020/2/25 14:35
 * Description:
 * History:
 **/
package com.wp.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.wp.gmall.beans.PaymentInfo;
import com.wp.gmall.mq.ActiveMQUtil;
import com.wp.gmall.payment.mapper.PaymentMapper;
import com.wp.gmall.service.PaymentService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ResponseBody;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentMapper paymentMapper;
    @Autowired
    private ActiveMQUtil activeMQUtil;
    @Autowired
    private AlipayClient alipayClient;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        //幂等性检查
        PaymentInfo paymentInfoParam = new PaymentInfo();
        paymentInfoParam.setOrderSn(paymentInfo.getOrderSn());
        PaymentInfo paymentInfoResult = paymentMapper.selectOne(paymentInfoParam);
        if (StringUtils.isNotBlank(paymentInfoResult.getPaymentStatus()) && paymentInfoResult.getPaymentStatus().equals("已支付")) {
            return;
        } else {
            String orderSn = paymentInfo.getOrderSn();
            Example example = new Example(PaymentInfo.class);
            example.createCriteria().andEqualTo("orderSn", orderSn);
            Connection connection = null;
            Session session = null;
            try {
                connection = activeMQUtil.getConnectionFactory().createConnection();
                session = connection.createSession(true, Session.SESSION_TRANSACTED);
            } catch (JMSException e) {
                e.printStackTrace();
            }

            try {
                paymentMapper.updateByExampleSelective(paymentInfo, example);
                Queue payment_success_queue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
                MessageProducer producer = session.createProducer(payment_success_queue);
                MapMessage message = new ActiveMQMapMessage();
                message.setString("out_trade_no", paymentInfo.getOrderSn());
                producer.send(message);
                session.commit();
            } catch (Exception e) {
                try {
                    session.rollback();
                } catch (JMSException e1) {
                    e1.printStackTrace();
                }
            } finally {
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void sendDelayPaymentResultCheckQueue(String outTradeNo, int count) {
        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
        } catch (JMSException e) {
            e.printStackTrace();
        }

        try {
            Queue payment_success_queue = session.createQueue("PAYMENT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(payment_success_queue);
            MapMessage message = new ActiveMQMapMessage();
            message.setString("out_trade_no", outTradeNo);
            //延迟消息队列，延迟半分钟
            message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, 1000 * 30);
            producer.send(message);
            session.commit();
        } catch (Exception e) {
            try {
                session.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Map<String, Object> checkAlipayPayment(String out_trade_no) {
        Map<String, Object> resultMap = new HashMap<>();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("out_trade_no", out_trade_no);
        request.setBizContent(JSON.toJSONString(requestMap));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            //参考Alipay API
            resultMap.put("out_trade_no", response.getOutTradeNo());
            resultMap.put("trade_no", response.getTradeNo());
            resultMap.put("trade_status", response.getTradeStatus());
            resultMap.put("call_back_content", response.getMsg());
        } else {
            System.out.println("调用失败！");
        }
        return resultMap;
    }
}
