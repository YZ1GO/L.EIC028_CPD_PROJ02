package com.noiatalk.services;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private static final Map<String, String> users = new HashMap<>();               // (username -> password)
    private static final Map<String, Boolean> loggedInUsers = new HashMap<>();      // username -> true/false (logged?)

    static {
        loadUsers();
    }

    private static void loadUsers() {
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
        }
    }

    public static boolean authenticate(String username, String password) {
        if (loggedInUsers.containsKey(username)) {
            return false;
        }

        String storedPassword = users.get(username);
        if (storedPassword != null && storedPassword.equals(password)) {
            loggedInUsers.put(username, true);
            return true;
        }

        return false;
    }

    public static boolean register(String username, String password) {
        if (users.containsKey(username)) {
            return false;
        }

        users.put(username, password);
        /*try(FileWriter writer = new FileWriter("config/users.cfg", true)) {
            writer.write(username + ":" + password + "\n");
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            return false;
        }*/

        return true;
    }

    public static void logout(String username) {
        loggedInUsers.remove(username);
    }
}

