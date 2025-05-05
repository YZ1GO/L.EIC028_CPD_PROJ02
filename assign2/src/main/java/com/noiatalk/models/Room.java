package com.noiatalk.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Room {
    private final String name;
    private final Set<String> users;
    private final boolean isSystem;
    private final boolean isAI;
    private final List<String> messageHistory = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

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
            messageHistory.add(message);
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
}