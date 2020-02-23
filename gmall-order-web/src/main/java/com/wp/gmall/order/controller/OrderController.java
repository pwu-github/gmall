/**
 * FileName: OrderController
 * Author: WP
 * Date: 2020/2/21 17:56
 * Description:
 * History:
 **/
package com.wp.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.wp.gmall.annotations.LoginRequired;
import com.wp.gmall.beans.OmsCartItem;
import com.wp.gmall.beans.OmsOrder;
import com.wp.gmall.beans.OmsOrderItem;
import com.wp.gmall.beans.UmsMemberReceiveAddress;
import com.wp.gmall.service.CartService;
import com.wp.gmall.service.OrderService;
import com.wp.gmall.service.SkuService;
import com.wp.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
@CrossOrigin
public class OrderController {
    @Reference
    private CartService cartService;
    @Reference
    private UserService userService;
    @Reference
    private OrderService orderService;
    @Reference
    private SkuService skuService;

    //提交订单
    @RequestMapping("/submitOrder")
    @LoginRequired(loginSuccess = true)
    public String submitOrder(String addressId,BigDecimal totalAmount,String tradeCode,HttpServletRequest request, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");
        //校验交易码
        String success = orderService.checkTradeCode(memberId,tradeCode);
        if(success.equals("success")){
            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            OmsOrder omsOrder = new OmsOrder();
            //设置订单属性
            omsOrder.setAutoConfirmDay(7);
            omsOrder.setCreateTime(new Date());
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickname);
            omsOrder.setNote("尽快发货！");
            String outTradeNo = "gmall";
            outTradeNo = outTradeNo + System.currentTimeMillis();
            outTradeNo = outTradeNo + new SimpleDateFormat("yyyyMMDDHHmmss").format(new Date());
            omsOrder.setOrderSn(outTradeNo);  //外部订单号，对接支付系统
            omsOrder.setPayAmount(totalAmount);
            omsOrder.setOrderType(1);
            UmsMemberReceiveAddress umsMemberReceiveAddress = userService.getAddressById(addressId);
            omsOrder.setReceiverCity(umsMemberReceiveAddress.getCity());
            omsOrder.setReceiverDetailAddress(umsMemberReceiveAddress.getDetailAddress());
            omsOrder.setReceiverName(umsMemberReceiveAddress.getName());
            omsOrder.setReceiverPhone(umsMemberReceiveAddress.getPhoneNumber());
            omsOrder.setReceiverPostCode(umsMemberReceiveAddress.getPostCode());
            omsOrder.setReceiverProvince(umsMemberReceiveAddress.getProvince());
            omsOrder.setReceiverRegion(umsMemberReceiveAddress.getRegion());
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 1);
            omsOrder.setReceiveTime(calendar.getTime());
            omsOrder.setSourceType(0);
            omsOrder.setStatus(0);
            omsOrder.setTotalAmount(totalAmount);

            //根据用户id查询购物车列表，获取商品信息 和总价格
            List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
            for (OmsCartItem omsCartItem : omsCartItems) {
                if(omsCartItem.getIsChecked().equals("1")){
                    OmsOrderItem omsOrderItem = new OmsOrderItem();
                    //检验价格,订单列表中的每一个item都需要核验价格
                    boolean b = skuService.checkPrice(omsCartItem.getProductSkuId(),omsCartItem.getPrice());
                    if(b == false){
                        return "tradeFail";
                    }
                    //设置订单列表商品属性
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductName(omsCartItem.getProductName());
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setRealAmount(omsCartItem.getTotalPrice());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductSkuCode("111111");
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    omsOrderItem.setProductId(omsCartItem.getProductId());
                    omsOrderItem.setProductSn("1234");  //仓库对应的商品编号

                    //检验库存

                    omsOrderItems.add(omsOrderItem);
                }
            }
            omsOrder.setOmsOrderItems(omsOrderItems);
            orderService.saveOrder(omsOrder);
            //跳转到支付系统
        }else{
            return "tradeFail";
        }

        return null;
    }

    //结算
    @RequestMapping("/toTrade")
    @LoginRequired(loginSuccess = true)
    public String toTrade(HttpServletRequest request, ModelMap modelMap) {

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        //收件人地址
        List<UmsMemberReceiveAddress> userAddressList = userService.getAddressByUserId(memberId);
        //购物车数据转化为订单页面清单
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        List<OmsOrderItem> omsOrderItems = new ArrayList<>();
        for (OmsCartItem omsCartItem : omsCartItems) {
            //选中的购物车商品才进入订单
            if (omsCartItem.getIsChecked().equals("1")) {
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());
                omsOrderItems.add(omsOrderItem);
            }
        }
        modelMap.put("omsOrderItems", omsOrderItems);
        modelMap.put("userAddressList", userAddressList);
        modelMap.put("totalAmount",getTotalAmount(omsCartItems));
        //生成交易码, 数据库和页面的交易码进行校验
        String tradeCode = orderService.genTradeCode(memberId);
        modelMap.put("tradeCode",tradeCode);
        return "trade";
    }

    //获得总金额
    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();
            //当被选中时，才计算到总价中
            if (omsCartItem.getIsChecked().equals("1")) {
                totalAmount = totalAmount.add(totalPrice);
            }
        }
        return totalAmount;
    }
}
