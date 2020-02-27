/**
 * FileName: OrderServiceImpl
 * Author: WP
 * Date: 2020/2/22 17:32
 * Description:
 * History:
 **/
package com.wp.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.wp.gmall.beans.OmsOrder;
import com.wp.gmall.beans.OmsOrderItem;
import com.wp.gmall.mq.ActiveMQUtil;
import com.wp.gmall.order.mapper.OmsOrderItemMapper;
import com.wp.gmall.order.mapper.OmsOrderMapper;
import com.wp.gmall.service.CartService;
import com.wp.gmall.service.OrderService;
import com.wp.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private OmsOrderMapper omsOrderMapper;
    @Autowired
    private OmsOrderItemMapper omsOrderItemMapper;
    @Reference
    private CartService cartService;
    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Override
    public String checkTradeCode(String memberId, String tradeCode) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String tradeCodeKey = "user:" + memberId + ":tradeCode";

            String tradeCodeFromCache = jedis.get(tradeCodeKey);
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeCodeKey), Collections.singletonList(tradeCode));

            if (eval != null && eval != 0) {
//                jedis.del(tradeCodeKey);
                return "success";
            } else {
                return "fail";
            }
        } finally {
            jedis.close();
        }
    }

    @Override
    public String genTradeCode(String memberId) {
        Jedis jedis = null;
        String tradeCode = null;
        try {
            jedis = redisUtil.getJedis();
            String tradeCodeKey = "user:" + memberId + ":tradeCode";
            tradeCode = UUID.randomUUID().toString();
            jedis.setex(tradeCodeKey, 60 * 15, tradeCode);
        } finally {
            jedis.close();
        }

        return tradeCode;
    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {
        //保存订单
        omsOrderMapper.insertSelective(omsOrder);
        //保存订单详情
        String id = omsOrder.getId();
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(id);
            omsOrderItemMapper.insertSelective(omsOrderItem);
            //在提交订单的同时，需要根据主键删除购物车中的数据。为了测试方便，先注释掉删除逻辑
//            cartService.delCartskuById(omsOrderItem.getProductSkuId());
        }
    }

    @Override
    public OmsOrder getOrderByOutTradeNo(String outTradeNo) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(outTradeNo);
        OmsOrder omsOrder1 = omsOrderMapper.selectOne(omsOrder);
        return omsOrder1;
    }

    @Override
    public void updateOrder(OmsOrder omsOrder) {
        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn",omsOrder.getOrderSn());
        OmsOrder omsOrder1 = new OmsOrder();
        omsOrder1.setStatus(1);

        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue order_pay_queue = session.createQueue("ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(order_pay_queue);
            MapMessage message = new ActiveMQMapMessage();
            omsOrderMapper.updateByExampleSelective(omsOrder1,example);
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
