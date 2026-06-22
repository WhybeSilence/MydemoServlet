package com.wang.servlet.entity;

import java.util.Date;

/**
 * 对应数据库表 audit_log
 */
public class AuditLog {
    private Integer logId;         // log_id (主键)
    private Integer productId;     // product_id (被审核的商品ID)
    private Integer adminId;       // admin_id (操作的管理员ID)
    private String action;         // action (操作类型: pass/reject)
    private String comment;        // comment (审核意见)
    private Date operateTime;      // operate_time (操作时间)

    public AuditLog() {}

    // Getters and Setters
    public Integer getLogId() { return logId; }
    public void setLogId(Integer logId) { this.logId = logId; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Date getOperateTime() { return operateTime; }
    public void setOperateTime(Date operateTime) { this.operateTime = operateTime; }
}