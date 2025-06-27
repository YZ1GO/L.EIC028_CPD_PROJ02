package com.noiatalk.services;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ConfigLoader {
    private static Map<String, String> config = new HashMap<>();

    public static void load(String filePath) throws Exception {
        System.out.println("Attempting to load config file from: " + filePath);
        if (!Files.exists(Paths.get(filePath))) {
            System.out.println("Config file does not exist at: " + filePath);
            return;
        }

        String content = new String(Files.readAllBytes(Paths.get(filePath)));

        // Simple logic to parse the JSON-like format
        content = content.trim();
        // Remove curly braces and split into lines
        if (content.startsWith("{") && content.endsWith("}")) {
            content = content.substring(1, content.length() - 1).trim();
        }

        String[] lines = content.split(",\\s*");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            // Remove quotes from around keys and values
            line = line.replaceAll("\"", "");
            int idx = line.indexOf(":");
            if (idx != -1) {
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                System.out.println("Loaded config: " + key + " = " + value);
                config.put(key, value);
            }
        }
    }

    public static String get(String key) {
        return config.getOrDefault(key, "");
    }
}
