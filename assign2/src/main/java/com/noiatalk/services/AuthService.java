package com.noiatalk.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AuthService {
    private static final Map<String, String> users = new HashMap<>();
    private static final Map<String, Boolean> loggedInUsers = new HashMap<>();
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    static {
        loadUsers();
    }

    private static void loadUsers() {
        lock.writeLock().lock();
        try {
            for (String line : Files.readAllLines(Path.of("config/users.cfg"))) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) continue;
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    users.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users file: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static boolean authenticate(String username, String password) {
        lock.writeLock().lock();
        try {
            if (loggedInUsers.containsKey(username)) {
                return false;
            }

            String storedPassword = users.get(username);
            if (storedPassword != null && storedPassword.equals(password)) {
                loggedInUsers.put(username, true);
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static boolean register(String username, String password) {
        lock.writeLock().lock();
        try {
            if (users.containsKey(username)) {
                return false;
            }
            users.put(username, password);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void logout(String username) {
        lock.writeLock().lock();
        try {
            loggedInUsers.remove(username);
        } finally {
            lock.writeLock().unlock();
        }
    }
}