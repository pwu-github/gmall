/**
 * FileName: UserServiceImpl
 * Author: WP
 * Date: 2020/2/6 11:06
 * Description:
 * History:
 **/
package com.wp.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.wp.gmall.beans.UmsMember;
import com.wp.gmall.beans.UmsMemberReceiveAddress;
import com.wp.gmall.service.UserService;
import com.wp.gmall.user.mapper.AddressMapper;
import com.wp.gmall.user.mapper.UserMapper;
import com.wp.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<UmsMember> getAllUser() {
//        List<UmsMember> umsMembers = userMapper.selectAllUser();
        List<UmsMember> umsMembers = userMapper.selectAll();
        return umsMembers;
    }

    @Override
    public List<UmsMemberReceiveAddress> getAddressByUserId(String userId) {
        Example example = new Example(UmsMemberReceiveAddress.class);
        //memberId是UmsMemberReceiveAddress表的外键字段
        example.createCriteria().andEqualTo("memberId",userId);
        List<UmsMemberReceiveAddress> addresses = addressMapper.selectByExample(example);
        return addresses;

        //或者用下面的查询方法,将传来的参数设置给 UmsMemberReceiveAddress 对象，mapper会根据对象中不为空的字段查询
//        UmsMemberReceiveAddress address = new UmsMemberReceiveAddress();
//        address.setMemberId(userId);
//        List<UmsMemberReceiveAddress> addresses = addressMapper.select(address);


    }

    public UmsMember login(UmsMember umsMember) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            if (jedis != null) {
                //从缓存中获取用户信息，根据密码对应用户信息
                String umsMemberStr = jedis.get("user:" + umsMember.getPassword() + umsMember.getUsername()+":info");
                if (StringUtils.isNotBlank(umsMemberStr)) {
                    //密码正确，解析成UmsMember对象
                    UmsMember umsMember1 = JSON.parseObject(umsMemberStr, UmsMember.class);
                    return umsMember1;
                }
            }
            //密码错误
            //缓存中没有或连接缓存失败，查询数据库，并刷新缓存
            UmsMember umsMember2 = loginFromDB(umsMember);
            if (umsMember2 != null) {
                jedis.setex("user:" + umsMember.getPassword() + umsMember.getUsername()+":info", 60 * 60 * 24, JSON.toJSONString(umsMember2));
            }
            return umsMember2;
        } finally {
            jedis.close();
        }
    }

    @Override
    public void addUserToken(String token, String memberId) {
        Jedis jedis = redisUtil.getJedis();
        jedis.setex("user:"+memberId+":token",60*60*2,token);
        jedis.close();
    }

    @Override
    public UmsMember addAuthoUser(UmsMember umsMember) {
        userMapper.insertSelective(umsMember);
        return umsMember;
    }

    @Override
    public UmsMember checkAutho2User(UmsMember umsMember) {
        UmsMember umsMember1 = userMapper.selectOne(umsMember);
        return umsMember1;
    }

    @Override
    public UmsMemberReceiveAddress getAddressById(String addressId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setId(addressId);
        UmsMemberReceiveAddress umsMemberReceiveAddress1 = addressMapper.selectOne(umsMemberReceiveAddress);
        return umsMemberReceiveAddress1;
    }

    private UmsMember loginFromDB(UmsMember umsMember) {

        List<UmsMember> umsMembers = userMapper.select(umsMember);
        if(umsMembers != null){
            return umsMembers.get(0);
        }
        return null;
    }
}
