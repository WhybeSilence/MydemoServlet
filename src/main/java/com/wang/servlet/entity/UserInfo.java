package com.wang.servlet.entity;

/**
 * 对应数据库表 user_info
 */
public class UserInfo {
    private Integer infoId;      // info_id (主键)
    private Integer userId;      // user_id (外键，关联 sys_user)
    private String nickname;     // nickname (昵称)
    private String avatar;       // avatar (头像路径)
    private String bio;          // bio (个人简介)

    public UserInfo() {}

    // Getters and Setters
    public Integer getInfoId() { return infoId; }
    public void setInfoId(Integer infoId) { this.infoId = infoId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}