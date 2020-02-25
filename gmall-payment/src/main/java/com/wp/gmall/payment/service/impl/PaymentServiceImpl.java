/**
 * FileName: PaymentServiceImpl
 * Author: WP
 * Date: 2020/2/25 14:35
 * Description:
 * History:
 **/
package com.wp.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.wp.gmall.beans.PaymentInfo;
import com.wp.gmall.payment.mapper.PaymentMapper;
import com.wp.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

@Service
public class PaymentServiceImpl implements PaymentService{
    @Autowired
    private PaymentMapper paymentMapper;
    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        String orderSn = paymentInfo.getOrderSn();
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderSn",orderSn);
        paymentMapper.updateByExampleSelective(paymentInfo,example);
    }
}
