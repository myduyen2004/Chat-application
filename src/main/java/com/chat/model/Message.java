package com.chatapp.model;

import java.time.LocalDateTime;

public class Message {
    private int messageId;
    private int senderId;
    private String senderName;
    private String content;
    private LocalDateTime sentAt;
    private Integer replyToId;
    private String replyContent;

    public Message() {
    }

    public Message(int messageId, int senderId, String content, LocalDateTime sentAt, Integer replyToId) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.content = content;
        this.sentAt = sentAt;
        this.replyToId = replyToId;
    }

    // Additional constructor with sender name for WebSocket communication
    public Message(int messageId, int senderId, String senderName, String content,
                   LocalDateTime sentAt, Integer replyToId, String replyContent) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.sentAt = sentAt;
        this.replyToId = replyToId;
        this.replyContent = replyContent;
    }

    // Getters and Setters
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

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public Integer getReplyToId() {
        return replyToId;
    }

    public void setReplyToId(Integer replyToId) {
        this.replyToId = replyToId;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + messageId +
                ", senderId=" + senderId +
                ", senderName='" + senderName + '\'' +
                ", content='" + content + '\'' +
                ", sentAt=" + sentAt +
                ", replyToId=" + replyToId +
                '}';
    }
}