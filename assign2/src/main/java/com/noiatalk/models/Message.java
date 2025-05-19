package com.noiatalk.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private String sender; // null if from system
    private String content;
    private LocalDateTime timestamp;
    private boolean isSystemMessage;

    public Message(String sender, String content, boolean isSystemMessage) {
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isSystemMessage = isSystemMessage;
    }

    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }

    public String getFormattedMessage() {
        if (isSystemMessage) {
            return "(" + content + ")";
        }
        return "[" + getFormattedTimestamp() + "] " + sender + ": " + content;
    }

    public boolean isSystemMessage() {
        return isSystemMessage;
    }

    public String getContent() {
        return content;
    }

    public String getSender() {
        return sender;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public static Message createSystemMessage(String content) {
        return new Message(null, content, true);
    }

    public static Message createUserMessage(String sender, String content) {
        return new Message(sender, content, false);
    }
}
