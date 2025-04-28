package com.example.questapplication;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    private String messageId;
    private String senderId;
    private String senderName; // 👈 Added this
    private String text;
    private String imageUrl;
    private long timestamp;

    public ChatMessage() {} // Needed for Firebase

    public ChatMessage(String messageId, String senderId, String senderName,
                       String text, String imageUrl, long timestamp) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getText() {
        return text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}



