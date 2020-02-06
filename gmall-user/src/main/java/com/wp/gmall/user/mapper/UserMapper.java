/**
 * FileName: UserMapper
 * Author: WP
 * Date: 2020/2/6 11:09
 * Description:
 * History:
 **/
package com.wp.gmall.user.mapper;

import com.wp.gmall.user.bean.UmsMember;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

//@Mapper
public interface UserMapper {

    List<UmsMember> selectAllUser();
}
