package com.example.savourit.models;

public class MessageModel {
    private String text;
    private long timestamp;

    // Required empty constructor for Firebase
    public MessageModel() {}

    public MessageModel(String text, long timestamp) {
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
