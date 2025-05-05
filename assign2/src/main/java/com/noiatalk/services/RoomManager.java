package com.noiatalk.services;

import com.noiatalk.models.Room;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RoomManager {
    private static final Map<String, Room> rooms = new HashMap<>();
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    static {
        lock.writeLock().lock();
        try {
            for (String line : Files.readAllLines(Path.of("config/rooms.cfg"))) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    rooms.put(line, new Room(line, true, false));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading rooms file: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }


    public static Set<String> getAvailableRooms() {
        lock.readLock().lock();
        try {
            return new HashSet<>(rooms.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }

    public static boolean isValidRoom(String roomName) {
        lock.readLock().lock();
        try {
            return rooms.containsKey(roomName);
        } finally {
            lock.readLock().unlock();
        }
    }

    public static Room getRoom(String roomName) {
        lock.readLock().lock();
        try {
            return rooms.get(roomName);
        } finally {
            lock.readLock().unlock();
        }
    }

    public static String getAvailableRoomsList() {
        lock.readLock().lock();
        try {
            StringBuilder list = new StringBuilder();
            for (String room : rooms.keySet()) {
                list.append("· ").append(room).append("\n");
            }
            return !list.isEmpty() ? list.toString() : "No rooms available.";
        } finally {
            lock.readLock().unlock();
        }
    }

    public static void createRoom(String roomName, boolean isAI) {
        lock.writeLock().lock();
        try {
            if (!rooms.containsKey(roomName)) {
                Room newRoom = new Room(roomName, false, isAI);
                rooms.put(roomName, newRoom);
                System.out.println("Room '" + roomName + "' created.");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void removeRoom(String roomName) {
        lock.writeLock().lock();
        try {
            if (rooms.containsKey(roomName)) {
                rooms.remove(roomName);
                System.out.println("Room '" + roomName + "' has been removed.");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}
