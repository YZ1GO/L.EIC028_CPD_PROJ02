package com.noiatalk.services;

import com.noiatalk.models.SessionData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AuthService {
    private static final Path USER_FILE = Path.of("config/users.cfg");
    private static final String HASH_ALGORITHM = "SHA-256";

    private static final Map<String, String> users = new HashMap<>();                   // username -> password_hash
    private static final Map<String, Boolean> loggedInUsers = new HashMap<>();
    private static final Map<String, SessionData> sessionTokens = new HashMap<>();      // token -> session data

    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    static {
        loadUsers();
    }

    private static void loadUsers() {
        lock.writeLock().lock();
        try {
            if (!Files.exists(USER_FILE)) {
                Files.createDirectories(USER_FILE.getParent());
                Files.createFile(USER_FILE);
                return;
            }

            Files.lines(USER_FILE)
                    .filter(line -> !line.startsWith("#") && !line.trim().isEmpty())
                    .forEach(line -> {
                        String[] parts = line.split(":");
                        if (parts.length == 2) {
                            users.put(parts[0], parts[1]);
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(password.getBytes());
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not available", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public static String authenticate(String username, String password) {
        lock.writeLock().lock();
        try {
            if (loggedInUsers.containsKey(username)) {
                return null;
            }

            String storedHash = users.get(username);
            if (storedHash == null) return null;

            String computedHash = hashPassword(password);
            if (storedHash.equals(computedHash)) {
                loggedInUsers.put(username, true);
                String token = UUID.randomUUID().toString();
                sessionTokens.put(token, new SessionData(username, null));
                return token;
            }
            return null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static String register(String username, String password) {
        lock.writeLock().lock();
        try {
            if (users.containsKey(username)) {
                return null;
            }

            String hash = hashPassword(password);
            users.put(username, hash);

            // Persist to file
            Files.writeString(USER_FILE,
                    username + ":" + hash + "\n",
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            loggedInUsers.put(username, true);
            String token = UUID.randomUUID().toString();
            sessionTokens.put(token, new SessionData(username, null));
            return token;
        } catch (IOException e) {
            System.err.println("Failed to register user: " + e.getMessage());
            users.remove(username); // Rollback
            return null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static SessionData reconnect(String token) {
        lock.writeLock().lock();
        try {
            SessionData session = sessionTokens.get(token);
            if (session == null || session.isExpired()) {
                return null;
            }
            if (loggedInUsers.containsKey(session.getUsername())) {
                return null;
            }
            loggedInUsers.put(session.getUsername(), true);
            return session;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static SessionData getSession(String token) {
        lock.readLock().lock();
        try {
            SessionData session = sessionTokens.get(token);
            if (session == null || session.isExpired()) {
                return null;
            }
            return session;
        } finally {
            lock.readLock().unlock();
        }
    }

    public static void logout(String username) {
        lock.writeLock().lock();
        try {
            loggedInUsers.remove(username);
            sessionTokens.entrySet().removeIf(entry -> entry.getValue().getUsername().equals(username));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void clearLoggedInUser(String username) {
        lock.writeLock().lock();
        try {
            loggedInUsers.remove(username);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void updateSessionRoom(String token, String roomName) {
        lock.writeLock().lock();
        try {
            SessionData session = sessionTokens.get(token);
            if (session != null) {
                session.setRoomName(roomName);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}