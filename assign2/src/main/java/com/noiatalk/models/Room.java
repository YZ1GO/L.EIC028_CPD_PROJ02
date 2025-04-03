package com.noiatalk.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Room {
    private final String name;
    private final Set<String> users;
    private final boolean isSystem;
    private final boolean isAI;
    private final List<String> messageHistory = new ArrayList<>();

    public Room(String name, boolean isSystem, boolean isAI) {
        this.name = name;
        this.isSystem = isSystem;
        this.isAI = isAI;
        this.users = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public synchronized void addUser(String username) {
        users.add(username);
    }

    public synchronized void removeUser(String username) {
        users.remove(username);
    }

    public synchronized boolean hasUser(String username) {
        return users.contains(username);
    }

    public synchronized Set<String> getUsers() {
        return new HashSet<>(users);
    }

    public synchronized boolean isSystem() {
        return isSystem;
    }

    public synchronized int getUserCount() {
        return users.size();
    }

    public synchronized boolean isAI() {
        return isAI;
    }

    public synchronized void addMessage(String message) {
        messageHistory.add(message);
    }

    public synchronized List<String> getMessageHistory() {
        return messageHistory;
    }
}
