package com.noiatalk;

import com.noiatalk.models.Message;
import com.noiatalk.models.Room;
import com.noiatalk.models.SessionData;
import com.noiatalk.services.AuthService;
import com.noiatalk.services.LLMService;
import com.noiatalk.services.RoomManager;

import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectionHandler implements Runnable {
    private final SSLSocket client;
    private final Server server;
    private final ReentrantLock roomLock = new ReentrantLock();
    private Room currentRoom;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private String sessionToken;

    private volatile boolean connected;
    private boolean isReconnection;

    public ConnectionHandler(SSLSocket client, Server server) {
        this.client = client;
        this.server = server;
        this.connected = true;
        this.isReconnection = false;
    }

    @Override
    public void run() {
        try {
            initializeStreams();
            if (authenticateUser()) {
                server.addConnection(this);
                enterLobby();
            }
        } catch (IOException e) {
            System.err.println("Connection error for " + username + ": " + e.getMessage());
        } finally {
            cleanupConnection();
        }
    }

    private void initializeStreams() throws IOException {
        //client.setSoTimeout(60000);
        out = new PrintWriter(client.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        client.startHandshake();
    }

    private boolean authenticateUser() throws IOException {
        while (connected) {
            sendMessage("Enter command: /login <username> <password>, /register <username> <password>, or /reconnect <token>");
            String input = in.readLine();
            if (input == null) {
                return false;
            }

            String[] parts = input.trim().split("\\s+");
            if (parts.length < 2) {
                sendMessage("Invalid command format. Try again.");
                continue;
            }

            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "/reconnect":
                        if (parts.length != 2) {
                            sendMessage("Usage: /reconnect <token>");
                            continue;
                        }
                        SessionData session = AuthService.reconnect(parts[1]);
                        if (session != null) {
                            this.username = session.getUsername();
                            this.sessionToken = parts[1];
                            this.isReconnection = true;
                            sendMessage("Reconnection successful. Welcome back, " + username + ". Your session token is: " + sessionToken);
                            return true;
                        }
                        sendMessage("Invalid, expired, or user already logged in.");
                        break;

                    case "/login":
                        if (parts.length != 3) {
                            sendMessage("Usage: /login <username> <password>");
                            continue;
                        }
                        String loginToken = AuthService.authenticate(parts[1], parts[2]);
                        if (loginToken != null) {
                            this.username = parts[1];
                            this.sessionToken = loginToken;
                            this.isReconnection = false;
                            sendMessage("Login successful. Welcome to the lobby. Your session token is: " + sessionToken);
                            return true;
                        }
                        sendMessage("Invalid credentials or user already logged in.");
                        break;

                    case "/register":
                        if (parts.length != 3) {
                            sendMessage("Usage: /register <username> <password>");
                            continue;
                        }
                        String registerToken = AuthService.register(parts[1], parts[2]);
                        if (registerToken != null) {
                            this.username = parts[1];
                            this.sessionToken = registerToken;
                            this.isReconnection = false;
                            sendMessage("Registration successful. You are now logged in. Your session token is: " + sessionToken);
                            return true;
                        }
                        sendMessage("Username already exists or invalid credentials.");
                        break;

                    default:
                        sendMessage("Unknown command. Use /login, /register, or /reconnect");
                }
            } catch (Exception e) {
                sendMessage("Authentication error: " + e.getMessage());
            }
        }
        return false;
    }

    private void enterLobby() throws IOException {
        currentRoom = null;

        if (isReconnection) {
            isReconnection = false;
            SessionData session = AuthService.getSession(sessionToken);
            if (session != null && session.getRoomName() != null && RoomManager.isValidRoom(session.getRoomName())) {
                System.out.println("Rejoining room for user: " + session.getUsername());
                if (joinRoom(session.getRoomName())) {
                    handleChat();
                    return;
                }
            }
            sendMessage("You are now in the lobby.");
        } else {
            displayRooms();
        }

        while (connected && currentRoom == null) {
            String input = promptInput("Enter a command (e.g., /join <roomname> or /create <roomname> [1 for AI room]):");
            if (input == null) {
                return;
            }

            input = input.trim();
            if (commandHandler(input)) {
                sendMessage("Invalid command. Use /join <roomname>, /create <roomname>, or /room list.");
            } else if (currentRoom != null) {
                handleChat();
                break;
            }
        }
    }

    private void displayRooms() {
        sendMessage("Available rooms:");
        sendMessage(RoomManager.getAvailableRoomsList());
    }

    private void handleChat() throws IOException {
        while (connected) {
            String message = in.readLine();
            if (message == null) {
                return;
            }

            message = message.trim();
            if (message.isEmpty()) continue;

            if (message.startsWith("/")) {
                if (commandHandler(message)) {
                    sendMessage("Invalid command. Try again.");
                }
                if (currentRoom == null) {
                    enterLobby();
                    break;
                }
            } else {
                Room room = getCurrentRoom();
                if (room != null) {
                    Message userMessage = Message.createUserMessage(username, message);
                    server.broadcast(userMessage, room);

                    if (room.isAI()) {
                        room.addMessage(username + ": " + message);
                    }
                } else {
                    sendMessage("You are not in a room. This should not happen!");
                }
            }
        }
    }

    private boolean commandHandler(String input) {
        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String argument = (parts.length > 1) ? parts[1].trim() : null;

        switch (command) {
            case "/logout":
                explicitLogout();
                return false;

            case "/room":
                if (argument != null && argument.equalsIgnoreCase("list")) {
                    displayRooms();
                    return false;
                }
                sendMessage("Usage: /room list");
                return false;

            case "/join":
                if (argument == null) {
                    sendMessage("Usage: /join <roomname>");
                    return false;
                }
                if (currentRoom == null) {
                    return !joinRoom(argument);
                } else {
                    boolean result = switchRoom(argument);
                    if (!result) {
                        sendMessage("You are already in " + currentRoom + " room.");
                    }
                    return false;
                }

            case "/create":
                if (argument == null) {
                    sendMessage("Usage: /create <roomname> [1 for AI room]");
                    return false;
                }
                return !createRoom(argument);

            case "/ai":
                if (argument == null) {
                    sendMessage("Usage: /ai <message>");
                    return false;
                }
                if (!handleAIMessage(argument)) {
                    sendMessage("This is not an AI-powered room.");
                    return false;
                }
                return true;

            case "/leave":
                if (leaveRoom()) {
                    sendMessage("You have left the room.");
                } else {
                    sendMessage("You are in lobby.");
                }
                return false;

            case "/info":
                if (currentRoom == null) return true;
                sendMessage(currentRoom.getRoomInfo());
                return false;

            default:
                return true;
        }
    }

    private boolean joinRoom(String roomName) {
        roomLock.lock();
        try {
            if (!RoomManager.isValidRoom(roomName)) {
                sendMessage("Room '" + roomName + "' does not exist.");
                return false;
            }
            Room room = RoomManager.getRoom(roomName);
            room.addUser(username);
            currentRoom = room;
            AuthService.updateSessionRoom(sessionToken, roomName);

            String message = String.format("You have joined room: %s", roomName);
            if (room.isAI()) {
                message += " (AI)";
            }
            sendMessage(message);

            server.broadcast(Message.createSystemMessage(username + " joined the room!"), room);
            return true;
        } finally {
            roomLock.unlock();
        }
    }

    private boolean leaveRoom() {
        roomLock.lock();
        try {
            if (currentRoom != null) {
                Room room = currentRoom;
                int userCount = room.getUserCount();
                room.removeUser(username);
                server.broadcast(Message.createSystemMessage(username + " left the room"), room);
                if (userCount == 1 && !room.isSystem()) {
                    RoomManager.removeRoom(room.getName());
                }
                currentRoom = null;
                AuthService.updateSessionRoom(sessionToken, null);
                return true;
            }
            return false;
        } finally {
            roomLock.unlock();
        }
    }

    private boolean switchRoom(String roomName) {
        if (Objects.equals(roomName, currentRoom.getName())) return false;
        leaveRoom();
        return joinRoom(roomName);
    }

    private boolean createRoom(String argument) {
        String[] args = argument.split("\\s+");
        String roomName = args[0].trim();
        boolean isAI = false;

        if (args.length > 1) {
            if (args[1].equals("1")) {
                isAI = true;
            } else if (!args[1].equals("0")) {
                sendMessage("Invalid second argument. Use 1 to create an AI room or 0 for regular room.");
                return false;
            }
        }

        if (RoomManager.isValidRoom(roomName)) {
            sendMessage("Room '" + roomName + "' already exists. Use /join <roomname> to enter.");
            return false;
        }

        RoomManager.createRoom(roomName, isAI);

        return switchRoom(roomName);
    }

    private boolean handleAIMessage(String userQuery) {
        Room room = getCurrentRoom();
        if (room == null || !room.isAI()) return false;

        try {
            String context = String.join("\n", room.getMessageHistory());
            String prompt = "Chat history:\n" + context + "\n\nUser: " + userQuery;

            String llmResponse = LLMService.getLLMResponseContent(userQuery, prompt);
            room.addMessage("BOT: " + llmResponse);

            Message aiMessage = Message.createUserMessage("BOT", llmResponse);
            server.broadcast(aiMessage, room);
        } catch (Exception e) {
            sendMessage("AI failed to respond: " + e.getMessage());
            System.err.println("AI error: " + e.getMessage());
        }
        return true;
    }

    private String promptInput(String prompt) throws IOException {
        sendMessage(prompt);
        return in.readLine();
    }

    public void shutdown() {
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (client != null && !client.isClosed()) client.close();
        } catch (IOException e) {
            System.err.println("Error closing connection for " + username + ": " + e.getMessage());
        }
    }

    public Room getCurrentRoom() {
        roomLock.lock();
        try {
            return currentRoom;
        } finally {
            roomLock.unlock();
        }
    }

    public void sendMessage(String message) {
        if (out != null && !out.checkError()) {
            out.println(message);
        }
    }

    private void explicitLogout() {
        if (username != null) {
            try {
                leaveRoom();
                AuthService.logout(username);
                server.removeConnection(this);
                System.out.println(username + " logged out");
            } finally {
                shutdown();
            }
        }
    }

    private void cleanupConnection() {
        if (username != null) {
            try {
                cleanupRoomMembership();
                AuthService.clearLoggedInUser(username);
                server.removeConnection(this);
                System.out.println(username + " disconnected");
            } finally {
                shutdown();
            }
        }
    }

    private void cleanupRoomMembership() {
        roomLock.lock();
        try {
            if (currentRoom != null) {
                Room room = currentRoom;
                int userCount = room.getUserCount();
                room.removeUser(username);
                server.broadcast(Message.createSystemMessage(username + " left the room"), room);
                if (userCount == 1 && !room.isSystem()) {
                    RoomManager.removeRoom(room.getName());
                }
                currentRoom = null;
            }
        } finally {
            roomLock.unlock();
        }
    }
}