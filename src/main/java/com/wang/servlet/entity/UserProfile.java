package com.wang.servlet.entity;

import java.util.Date;

/**
 * 对应数据库表 user_profile
 */
public class UserProfile {
    private Integer profileId;    // profile_id (自增 ID，主键)
    private Integer userId;       // user_id (关联 sys_user 表的 user_id)
    private String avatarUrl;     // avatar_url (头像 URL)
    private String bio;           // bio (个人简介/签名)
    private Date updateTime;      // update_time (资料更新时间)

    public UserProfile() {}

    // Getters and Setters
    public Integer getProfileId() {
        return profileId;
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}