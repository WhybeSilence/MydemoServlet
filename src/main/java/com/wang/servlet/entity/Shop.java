package com.wang.servlet.entity;

/**
 * 对应数据库表 shop
 */
public class Shop {
    private Integer shopId;        // shop_id (主键)
    private Integer ownerId;       // owner_id (店主ID，关联 sys_user)
    private String shopName;       // shop_name (店铺名称)
    private String description;    // description (店铺描述)

    public Shop() {}

    // Getters and Setters
    public Integer getShopId() { return shopId; }
    public void setShopId(Integer shopId) { this.shopId = shopId; }

    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer ownerId) { this.ownerId = ownerId; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}