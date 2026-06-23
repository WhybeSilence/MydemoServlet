package com.wang.servlet.entity;

import java.util.Date;

/**
 * 对应数据库表 shop
 */
public class Shop {
    private Integer shopId;       // shop_id (自增 ID，主键)
    private Integer ownerId;      // owner_id (关联 sys_user 表，店主 ID)
    private String shopName;      // shop_name (店铺名称)
    private String description;   // description (店铺介绍)
    private String shopImg;       // shop_img (店铺展示图片 URL)
    private Integer status;       // status (营业状态: 0-休息中, 1-营业中)
    private Date createTime;      // create_time (开店时间)

    public Shop() {}

    // Getters and Setters
    public Integer getShopId() {
        return shopId;
    }

    public void setShopId(Integer shopId) {
        this.shopId = shopId;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShopImg() {
        return shopImg;
    }

    public void setShopImg(String shopImg) {
        this.shopImg = shopImg;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}