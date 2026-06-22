package com.wang.servlet.entity;

/**
 * 对应数据库表 product
 */
public class Product {
    private Integer productId;     // product_id (主键)
    private Integer shopId;        // shop_id (所属店铺ID)
    private String name;           // name (商品名称)
    private Double price;          // price (价格)
    private Integer stock;         // stock (库存)
    private Integer status;        // status (状态: 0-下架, 1-上架, 2-审核中等)
    private String detail;         // detail (商品详情描述)

    public Product() {}

    // Getters and Setters
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getShopId() { return shopId; }
    public void setShopId(Integer shopId) { this.shopId = shopId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
}