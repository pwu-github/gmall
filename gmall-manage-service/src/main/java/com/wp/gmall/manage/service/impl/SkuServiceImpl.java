/**
 * FileName: SkuServiceImpl
 * Author: WP
 * Date: 2020/2/10 19:15
 * Description:
 * History:
 **/
package com.wp.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.wp.gmall.beans.PmsSkuAttrValue;
import com.wp.gmall.beans.PmsSkuImage;
import com.wp.gmall.beans.PmsSkuInfo;
import com.wp.gmall.beans.PmsSkuSaleAttrValue;
import com.wp.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.wp.gmall.manage.mapper.PmsSkuImageMapper;
import com.wp.gmall.manage.mapper.PmsSkuInfoMapper;
import com.wp.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.wp.gmall.service.SkuService;
import com.wp.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.util.StringUtil;

import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    private PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    private PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    private PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void savaSkuInfo(PmsSkuInfo pmsSkuInfo) {
        //保存skuInfo
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        //一定要在执行了保存操作后，才会主键返回策略，获得skuId
        String skuId = pmsSkuInfo.getId();
        //保存平台关联属性 base_attr
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }
        //保存销售属性
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }
        //保存图片
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }
    }

    public PmsSkuInfo getSkuByIdFromDB(String skuId) {
        //查询skuInfo
        PmsSkuInfo skuInfo = new PmsSkuInfo();
        skuInfo.setId(skuId);
        PmsSkuInfo pmsSkuInfo = pmsSkuInfoMapper.selectOne(skuInfo);
        //查询图片
        PmsSkuImage skuImage = new PmsSkuImage();
        skuImage.setSkuId(skuId);
        List<PmsSkuImage> skuImages = pmsSkuImageMapper.select(skuImage);
        pmsSkuInfo.setSkuImageList(skuImages);
        return pmsSkuInfo;
    }

    //使用Redis缓存查询
    @Override
    public PmsSkuInfo getSkuById(String skuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        //连接Redis
        Jedis jedis = redisUtil.getJedis();
        //查询缓存
        String skuKey = "sku:" + skuId + ":info";  //缓存策略
        //根据skuKey获得一个json字符串
        String s = jedis.get(skuKey);
        if (StringUtils.isNotBlank(s)) {   //if(s != null && s != "")
            //将json字符串解析成java对象
            pmsSkuInfo = JSON.parseObject(s, PmsSkuInfo.class);
        } else {
            //如果缓存中没有，查询MySQL，查询MySQL之前，需要设置分布式锁，防止缓存失效产生缓存击穿,10S的过期时间
            String token = UUID.randomUUID().toString();
            String ok = jedis.set("sku:" + skuId + ":lock", token, "nx", "px", 10 * 1000);
            if (StringUtils.isNotBlank(ok) && ok.equals("OK")) {
                //设置成功，有权在或其时间内访问数据库
                pmsSkuInfo = getSkuByIdFromDB(skuId);
                if (pmsSkuInfo != null) {
                    //MySQL查询结果放入缓存
                    jedis.set("sku:" + skuId + ":info", JSON.toJSONString(pmsSkuInfo));
                } else {
                    //如果MySQL中不存在 pmsSkuInfo
                    //为了防止缓存穿透，给不存在的对象在redis中设置一个空的value.同时，设置一个过期时间 3分钟
                    //缓存穿透：高并发场景下，大量访问缓存中不存在的数据，绕过缓存直达数据库，导致数据库压力增大
                    jedis.setex("sku:" + skuId + ":info", 60 * 3, JSON.toJSONString(""));
                }
                //访问完MySQL后，需要释放锁。验证token，防止删除了其他线程的锁，保证删除的是当前线程自己的锁
                String lockToken = jedis.get("sku:" + skuId + ":lock");
                //如果在获得token后，还没来得及删除就过期了，那就用lua脚本，在get token的同时就删除掉锁
                if(StringUtils.isNotBlank(lockToken)&&lockToken.equals(token)){
                    jedis.del("sku:" + skuId + ":lock");
                }

            } else {
                //设置失败，已经有线程占用了锁，此时别的线程 自旋（线程睡眠一段时间后，继续访问该线程）
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /*
                如果直接调用getSkuById(skuId); 则不是自旋，只是多开了一个线程，没得到锁的线程仍然无法访问数据库。
                加了return关键字，才是在同一个线程中睡眠一段时间后继续访问资源，这才是自旋。
                **/
                return getSkuById(skuId);
            }
        }
        //关闭资源
        jedis.close();
        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);
        return pmsSkuInfos;
    }

    @Override
    public List<PmsSkuInfo> getAllSku() {
        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
            List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuAttrValueMapper.selectAll();
            pmsSkuInfo.setSkuAttrValueList(pmsSkuAttrValues);
        }
        return pmsSkuInfos;
    }
}
