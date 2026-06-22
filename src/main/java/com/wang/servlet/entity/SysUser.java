package com.wang.servlet.entity;

import java.util.Date;

/**
 * 对应数据库表 sys_user
 */
public class SysUser {
    private Integer userId;      // user_id (主键)
    private String username;     // username (登录名)
    private String password;     // password (加密密码)
    private String userRole;     // user_role (角色: admin, shop_owner, user)
    private Date createTime;     // create_time (创建时间)

    public SysUser() {}

    // Getters and Setters
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}