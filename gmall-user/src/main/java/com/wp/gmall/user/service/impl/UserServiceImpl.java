/**
 * FileName: UserServiceImpl
 * Author: WP
 * Date: 2020/2/6 11:06
 * Description:
 * History:
 **/
package com.wp.gmall.user.service.impl;

import com.wp.gmall.user.bean.UmsMember;
import com.wp.gmall.user.mapper.UserMapper;
import com.wp.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<UmsMember> getAllUser() {
        List<UmsMember> umsMembers = userMapper.selectAllUser();
        return umsMembers;
    }
}
