package com.noiatalk.services;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class LLMService {
    private static String LLM_URL;
    private static String MODEL;

    // Load configuration on class initialization
    static {
        try {
            ConfigLoader.load("config.json");
            LLM_URL = ConfigLoader.get("LLM_URL");
            MODEL = ConfigLoader.get("MODEL");

            if (LLM_URL == null || MODEL == null) {
                throw new IllegalStateException("LLM credentials (LLM_URL, MODEL) are not set in configuration.");
            }

            // Ensure URL ends with / if it doesn't have a path
            if (!LLM_URL.endsWith("/") && !LLM_URL.contains("/v1")) {
                LLM_URL += "/";
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize LLMService", e);
        }
    }

    public static String getLLMResponseContent(String userQuery, String prompt) {
        HttpURLConnection connection = null;
        try {
            // Build the JSON payload manually (without external libraries)
            String jsonPayload = buildJsonPayload(userQuery, prompt);

            // Create connection
            URL url = new URL(LLM_URL + "v1/chat/completions"); // Adjust endpoint as needed
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(30000);

            // Send request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Handle response
            int statusCode = connection.getResponseCode();
            if (statusCode == 200) {
                try (InputStream is = connection.getInputStream();
                     BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String response = br.lines().collect(Collectors.joining());
                    return extractContent(response);
                }
            } else {
                String errorResponse = readErrorStream(connection);
                System.err.println("LLM API Error - Status: " + statusCode + ", Response: " + errorResponse);
                return "[LLM Error] API request failed with status " + statusCode;
            }
        } catch (Exception e) {
            System.err.println("LLM Service Exception: " + e.getMessage());
            return "[LLM Error] " + e.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String buildJsonPayload(String userQuery, String prompt) {
        // Escape JSON special characters in the content
        String escapedPrompt = escapeJson(prompt);
        String escapedQuery = escapeJson(userQuery);

        return "{" +
                "\"model\":\"" + MODEL + "\"," +
                "\"messages\":[" +
                "{\"role\":\"system\",\"content\":\"" + escapedPrompt + "\"}," +
                "{\"role\":\"user\",\"content\":\"" + escapedQuery + "\"}" +
                "]," +
                "\"temperature\":0.7" +
                "}";
    }

    private static String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String extractContent(String json) {
        json = json.trim();
        String key = "\"content\":";
        int keyIndex = json.indexOf(key);

        if (keyIndex == -1) {
            return "Content not found";
        }

        int start = json.indexOf('"', keyIndex + key.length()) + 1;
        int end = json.indexOf('"', start);

        if (start == 0 || end == -1) {
            return "Invalid JSON format";
        }

        return json.substring(start, end);
    }

    private static String readErrorStream(HttpURLConnection connection) {
        try (InputStream es = connection.getErrorStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(es, StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining());
        } catch (Exception e) {
            return "Could not read error stream: " + e.getMessage();
        }
    }
}

