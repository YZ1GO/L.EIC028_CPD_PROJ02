package com.noiatalk.models;

import java.util.HashSet;
import java.util.Set;

public class Room {
    private String name;
    private final Set<String> users;
    private final boolean isSystem;

    public Room(String name, boolean isSystem) {
        this.name = name;
        this.isSystem = isSystem;
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

    public boolean isSystem() {
        return isSystem;
    }

    public int getUserCount() {
        return users.size();
    }
}
