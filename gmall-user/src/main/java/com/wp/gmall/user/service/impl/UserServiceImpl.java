/**
 * FileName: UserServiceImpl
 * Author: WP
 * Date: 2020/2/6 11:06
 * Description:
 * History:
 **/
package com.wp.gmall.user.service.impl;

import com.wp.gmall.beans.UmsMember;
import com.wp.gmall.beans.UmsMemberReceiveAddress;
import com.wp.gmall.user.mapper.AddressMapper;
import com.wp.gmall.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wp.gmall.service.UserService;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AddressMapper addressMapper;

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
}
