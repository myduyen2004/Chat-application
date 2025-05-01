package com.chat.model;

import java.util.Date;

public class Message {
    private int messageId;
    private int senderId;
    private String senderName;
    private String content;
    private Date sentTime;
    private Integer repliedToId;
    private boolean isSticker;
    private String attachmentUrl;
    private String repliedContent; // Để hiển thị nội dung tin nhắn được reply

    public Message() {
    }

    public Message(int senderId, String content) {
        this.senderId = senderId;
        this.content = content;
        this.sentTime = new Date();
    }

    // Constructor đầy đủ
    public Message(int senderId, String senderName, String content, Integer repliedToId,
                   boolean isSticker, String attachmentUrl) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.sentTime = new Date();
        this.repliedToId = repliedToId;
        this.isSticker = isSticker;
        this.attachmentUrl = attachmentUrl;
    }

    // Getters và setters
    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getSentTime() {
        return sentTime;
    }

    public void setSentTime(Date sentTime) {
        this.sentTime = sentTime;
    }

    public Integer getRepliedToId() {
        return repliedToId;
    }

    public void setRepliedToId(Integer repliedToId) {
        this.repliedToId = repliedToId;
    }

    public boolean isSticker() {
        return isSticker;
    }

    public void setSticker(boolean sticker) {
        isSticker = sticker;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public String getRepliedContent() {
        return repliedContent;
    }

    public void setRepliedContent(String repliedContent) {
        this.repliedContent = repliedContent;
    }
}