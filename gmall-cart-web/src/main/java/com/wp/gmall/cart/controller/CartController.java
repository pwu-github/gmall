/**
 * FileName: CartController
 * Author: WP
 * Date: 2020/2/17 21:14
 * Description:
 * History:
 **/
package com.wp.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.wp.gmall.beans.OmsCartItem;
import com.wp.gmall.beans.PmsSkuInfo;
import com.wp.gmall.service.CartService;
import com.wp.gmall.service.SkuService;
import com.wp.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@CrossOrigin
public class CartController {

    @Reference
    private SkuService skuService;
    @Reference
    private CartService cartService;

    //购物车列表
    @RequestMapping("/cartList")
    public String cartList(HttpServletRequest request,ModelMap modelMap) {
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        String memberId = "1";
        if (StringUtils.isNotBlank(memberId)) {
            //如果已经登录，查询数据库
            omsCartItems = cartService.cartList(memberId);
        } else {
            //如果没有登陆，查询cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(StringUtils.isNotBlank(cartListCookie)){
                omsCartItems = JSON.parseArray(cartListCookie,OmsCartItem.class);
            }
        }

        for (OmsCartItem omsCartItem : omsCartItems) {
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
        }
        modelMap.put("cartList", omsCartItems);
        //计算勾选商品的总价格
        BigDecimal totalAmount = getTotalAmount(omsCartItems);
        modelMap.put("totalAmount",totalAmount);
        return "cartList";
    }

    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();
            //当被选中时，才计算到总价中
            if(omsCartItem.getIsChecked().equals("1")){
                totalAmount = totalAmount.add(totalPrice);
            }
        }
        return totalAmount;
    }

    @RequestMapping("/checkCart")
    public String checkCart(String isChecked,String skuId,ModelMap modelMap){
        String memberId = "1";
        //修改状态
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setMemberId(memberId);
        omsCartItem.setIsChecked(isChecked);
        cartService.checkCart(omsCartItem);
        //查询最新数据，渲染内嵌页面
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        modelMap.put("cartList",omsCartItems);
        //计算勾选商品的总价格
        BigDecimal totalAmount = getTotalAmount(omsCartItems);
        modelMap.put("totalAmount",totalAmount);
        return "cartListInner";
    }

    //添加购物车，页面传递skuId和quantity
    @RequestMapping("/addToCart")
    public String addToCart(String skuId, int quantity, HttpServletRequest request, HttpServletResponse response) {
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        //查询商品信息
        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId);
        //封装购物车信息
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(pmsSkuInfo.getPrice());
        omsCartItem.setProductCategoryId(pmsSkuInfo.getCatalog3Id());
        omsCartItem.setProductId(pmsSkuInfo.getProductId());
        omsCartItem.setProductName(pmsSkuInfo.getSkuName());
        omsCartItem.setProductPic(pmsSkuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("1111111");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(new BigDecimal(quantity));
        //判断用户是否登录
        String memberId = "1";
        if (StringUtils.isBlank(memberId)) {
            //用户未登录，购物车数据存放在cookie中
            //cookie中原有的数据
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isBlank(cartListCookie)) {
                //cookie空
                omsCartItems.add(omsCartItem);
            } else {
                //cookie不为空，比较新增的购物车数据是否已经存在
                omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                //新增的购物车数据 是否已经存在
                boolean exist = cartExit(omsCartItems, omsCartItem);
                if (exist) {
                    //已经存在，更新购物车中同款商品的数量
                    for (OmsCartItem cartItem : omsCartItems) {
                        cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                    }
                } else {
                    //不存在，新增
                    omsCartItems.add(omsCartItem);
                }
            }
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItems), 60 * 60 * 72, true);
        } else {
            //用户已登录
            OmsCartItem omsCartItemFromDb = cartService.cartExitByUser(memberId, skuId);

            if (omsCartItemFromDb == null) {
                //没有添加过该商品
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname("xiaoming");
                omsCartItem.setQuantity(new BigDecimal(quantity));
                cartService.addCart(omsCartItem);

            } else {
                //添加过该商品
                omsCartItemFromDb.setQuantity(omsCartItemFromDb.getQuantity().add(omsCartItem.getQuantity()));
                cartService.updateCart(omsCartItemFromDb);
            }

            //同步缓存
            cartService.flushCartCache(memberId);

        }
        return "redirect:/success.html";
    }

    private boolean cartExit(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        boolean b = false;
        for (OmsCartItem cartItem : omsCartItems) {
            String productSkuId = cartItem.getProductSkuId();
            if (productSkuId.equals(omsCartItem.getProductId())) {
                b = true;
            }
        }
        return b;
    }
}
