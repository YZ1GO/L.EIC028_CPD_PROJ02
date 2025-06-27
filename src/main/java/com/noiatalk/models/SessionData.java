package com.noiatalk.models;

public class SessionData {
    private final String username;
    private String roomName;

    // milliseconds
    private final long expiryTime;

    // 30 mins
    private static final long TOKEN_EXPIRY_MS = 30 * 60 * 1000;

    public SessionData(String username, String roomName) {
        this.username = username;
        this.roomName = roomName;
        this.expiryTime = System.currentTimeMillis() + TOKEN_EXPIRY_MS;
    }

    public String getUsername() {
        return username;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}