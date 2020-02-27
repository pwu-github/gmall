/**
 * FileName: PaymentController
 * Author: WP
 * Date: 2020/2/24 14:27
 * Description:
 * History:
 **/
package com.wp.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.PosBillPayChannel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.wp.gmall.annotations.LoginRequired;
import com.wp.gmall.beans.OmsOrder;
import com.wp.gmall.beans.PaymentInfo;
import com.wp.gmall.payment.config.AlipayConfig;
import com.wp.gmall.service.OrderService;
import com.wp.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@CrossOrigin
public class PaymentController {
    @Autowired
    private AlipayClient alipayClient;
    @Autowired
    private OrderService orderService;
    @Reference
    private PaymentService paymentService;

    @RequestMapping("/alipay/callback/return")
    @LoginRequired(loginSuccess = true)
    public String alipayCallBackReturn(HttpServletRequest request, ModelMap modelMap) {

        //回调请求支付宝参数
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String total_amount = request.getParameter("total_amount");
        String subject = request.getParameter("subject");
        String call_back_content = request.getQueryString();

        if(StringUtils.isNotBlank(sign)){
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setAlipayTradeNo(trade_no);
            paymentInfo.setCallbackContent(call_back_content);
            paymentInfo.setCallbackTime(new Date());
            //更新支付信息
            paymentService.updatePayment(paymentInfo);
        }
        return "finish";
    }

    @RequestMapping("/index")
    @LoginRequired(loginSuccess = true)
    public String index(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        modelMap.put("outTradeNo",outTradeNo);
        modelMap.put("totalAmount",totalAmount);
        modelMap.put("nickname",nickname);
        return "index";
    }

    @RequestMapping("/wxPay/submit")
    @LoginRequired(loginSuccess = true)
    public String wxPay(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap) {

        return "index";
    }

    @RequestMapping("/alipay/submit")
    @LoginRequired(loginSuccess = true)
    public String alipay(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap) {

        String form = null;
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();

        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",outTradeNo);
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",totalAmount);
        map.put("subject","iphone7 32G");
        String param = JSON.toJSONString(map);
        alipayRequest.setBizContent(param);
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();

        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //保存支付信息
        OmsOrder omsOrder = orderService.getOrderByOutTradeNo(outTradeNo);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(outTradeNo);
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("gmall商城");
        paymentInfo.setTotalAmount(totalAmount);
        paymentService.savePaymentInfo(paymentInfo);

        paymentService.sendDelayPaymentResultCheckQueue(outTradeNo,5);
        return form;
    }
}
