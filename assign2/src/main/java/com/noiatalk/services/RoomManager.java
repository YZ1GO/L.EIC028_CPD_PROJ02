package com.noiatalk.services;

import com.noiatalk.models.Room;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class RoomManager {
    private static final Map<String, Room> activeRooms = new HashMap<>();

    static {
        loadRooms();
    }

    private static void loadRooms() {
        try {
            for (String line : Files.readAllLines(Path.of("config/rooms.cfg"))) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    activeRooms.put(line, new Room(line));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading rooms file: " + e.getMessage());
        }
    }

    public static Set<String> getAvailableRooms() {
        return activeRooms.keySet();
    }

    public static boolean isValidRoom(String roomName) {
        return activeRooms.containsKey(roomName);
    }

    public static Room getRoom(String roomName) {
        return activeRooms.get(roomName);
    }

    public static String getAvailableRoomsList() {
        StringBuilder list = new StringBuilder();
        for (String room : RoomManager.getAvailableRooms()) {
            list.append("- ").append(room);
            list.append("\n");
        }

        return list.toString();
    }
}
