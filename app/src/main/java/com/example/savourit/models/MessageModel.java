package com.example.savourit.models;

public class MessageModel {
    private String senderId;
    private String text;
    private long timestamp;
    private String senderUsername;

    public MessageModel() {
        // Required for Firestore
    }

    public MessageModel(String senderId, String senderUsername, String text, long timestamp) {
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public String getSenderUsername() {
        return senderUsername;
    }
}
