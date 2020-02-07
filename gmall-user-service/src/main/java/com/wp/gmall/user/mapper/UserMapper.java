/**
 * FileName: UserMapper
 * Author: WP
 * Date: 2020/2/6 11:09
 * Description:
 * History:
 **/
package com.wp.gmall.user.mapper;

import com.wp.gmall.beans.UmsMember;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

//@Mapper
public interface UserMapper extends Mapper<UmsMember> {

    List<UmsMember> selectAllUser();
}
