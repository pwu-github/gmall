/**
 * FileName: UmsMember
 * Author: WP
 * Date: 2020/2/6 12:13
 * Description:
 * History:
 **/
package com.wp.gmall.user.bean;

import lombok.Data;

import java.util.Date;

@Data
public class UmsMember {
    private String id;
    private String memberLevelId;
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private Integer status;
    private Date createTime;
    private String icon;
    private Integer gender;
    private Date birthday;
    private String city;
    private String job;
    private String personalizedSignature;
    private Integer sourceType;
    private Integer integration;
    private Integer growth;
    private Integer luckeyCount;
    private Integer historyIntegration;
}
