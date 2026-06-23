package com.wang.servlet.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 对应数据库表 product
 */
public class Product {
    private Integer productId;      // product_id (自增 ID，主键)
    private Integer shopId;         // shop_id (关联 shop 表的 shop_id)
    private String name;            // name (商品名称)
    private BigDecimal price;       // price (价格，保留两位小数)
    private Integer stock;          // stock (库存数量，预约后需 -1)
    private String description;     // description (商品详情描述)
    private String previewUrl;      // preview_url (商品预览图 URL)
    private Integer auditStatus;    // audit_status (审核状态: 0:待审, 1:通过, 2:拒绝)
    private String rejectReason;    // reject_reason (拒绝原因，当 status=2 时显示)
    private Date uploadTime;        // upload_time (上传/申请时间)

    public Product() {}

    // Getters and Setters
    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getShopId() {
        return shopId;
    }

    public void setShopId(Integer shopId) {
        this.shopId = shopId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Date getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Date uploadTime) {
        this.uploadTime = uploadTime;
    }
}