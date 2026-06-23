package com.wang.servlet.entity;

import java.util.Date;

/**
 * 对应数据库表 system_message
 */
public class SystemMessage {
    private Integer msgId;        // msg_id (主键，自增)
    private Integer receiverId;   // receiver_id (外键，接收者 ID，通常是店主 user_id)
    private String content;       // content (消息内容)
    private Integer isRead;       // is_read (是否已读: 0-未读, 1-已读)
    private Date createTime;      // create_time (消息生成时间)

    public SystemMessage() {}

    // Getters and Setters
    public Integer getMsgId() {
        return msgId;
    }

    public void setMsgId(Integer msgId) {
        this.msgId = msgId;
    }

    public Integer getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}