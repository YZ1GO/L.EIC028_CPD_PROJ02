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

    public void addUser(String username) {
        users.add(username);
    }

    public void removeUser(String username) {
        users.remove(username);
    }

    public boolean hasUser(String username) {
        return users.contains(username);
    }

    public Set<String> getUsers() {
        return new HashSet<>(users);
    }

    public boolean isSystem() {
        return isSystem;
    }

    public int getUserCount() {
        return users.size();
    }

    public boolean isAI() {
        return isAI;
    }

    public void addMessage(String message) {
        messageHistory.add(message);
    }

    public List<String> getMessageHistory() {
        return messageHistory;
    }
}
