package com.wp.gmall.service;


import com.wp.gmall.beans.UmsMember;
import com.wp.gmall.beans.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {

    List<UmsMember> getAllUser();

    List<UmsMemberReceiveAddress> getAddressByUserId(String userId);

    UmsMember login(UmsMember umsMember);

    void addUserToken(String token, String memberId);
}
