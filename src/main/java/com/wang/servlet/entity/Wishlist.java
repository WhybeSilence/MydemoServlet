package com.wang.servlet.entity;

import java.util.Date;

/**
 * 对应数据库表 wishlist
 */
public class Wishlist {
    private Integer id;           // id (自增 ID，主键)
    private Integer userId;       // user_id (关联 sys_user 表)
    private Integer productId;    // product_id (关联 product 表)
    private Date addTime;         // add_time (收藏时间)

    public Wishlist() {}

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }
}