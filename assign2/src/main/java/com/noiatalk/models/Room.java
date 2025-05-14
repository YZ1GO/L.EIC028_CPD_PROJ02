package com.noiatalk.models;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Room {
    private final String name;
    private final Set<String> users;
    private final boolean isSystem;
    private final boolean isAI;
    private final Deque<String> messageHistory = new LinkedList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private static final int MAX_HISTORY_SIZE = 100;

    public Room(String name, boolean isSystem, boolean isAI) {
        this.name = name;
        this.isSystem = isSystem;
        this.isAI = isAI;
        this.users = new HashSet<>();
    }

    public String getName() {
        lock.readLock().lock();
        try {
            return name;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addUser(String username) {
        lock.writeLock().lock();
        try {
            users.add(username);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeUser(String username) {
        lock.writeLock().lock();
        try {
            users.remove(username);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean hasUser(String username) {
        lock.readLock().lock();
        try {
            return users.contains(username);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Set<String> getUsers() {
        lock.readLock().lock();
        try {
            return new HashSet<>(users);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isSystem() {
        lock.readLock().lock();
        try {
            return isSystem;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getUserCount() {
        lock.readLock().lock();
        try {
            return users.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isAI() {
        lock.readLock().lock();
        try {
            return isAI;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addMessage(String message) {
        lock.writeLock().lock();
        try {
            if (messageHistory.size() >= MAX_HISTORY_SIZE) {
                messageHistory.removeFirst();
            }
            messageHistory.addLast(message);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<String> getMessageHistory() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(messageHistory);
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getRoomInfo() {
        lock.readLock().lock();
        try {
            return String.format(
                    "==== ROOM INFORMATION ====\nName: %s\nType: %s\nAI-powered: %s\nUsers Online: %d\n===========================",
                    name,
                    isSystem ? "System" : "User-created",
                    isAI ? "Yes" : "No",
                    users.size()
            );
        } finally {
            lock.readLock().unlock();
        }
    }
}