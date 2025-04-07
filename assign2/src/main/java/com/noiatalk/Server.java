package com.noiatalk;

import com.noiatalk.models.Message;
import com.noiatalk.models.Room;
import com.noiatalk.services.AuthService;
import com.noiatalk.services.ConfigLoader;
import com.noiatalk.services.LLMService;
import com.noiatalk.services.RoomManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private final ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private ExecutorService pool;
    private boolean done;

    public Server() {
        done = false;
        connections = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            ConfigLoader.load("config.json");

            String portStr = ConfigLoader.get("SOCKET_PORT");
            int port = (portStr != null) ? Integer.parseInt(portStr) : 9999;

            server = new ServerSocket(port);
            System.out.println("Server started successfully on port " + port + "\n");

            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                pool.execute(handler);
            }
        } catch (Exception e) {
            shutdown();
        }
    }

    public void broadcast(Message message, Room room) {
        if (room == null) return;

        String formatted = message.getFormattedMessage();
        for (ConnectionHandler ch : connections) {
            if (ch != null && ch.getCurrentRoom() != null && ch.getCurrentRoom().equals(room)) {
                ch.sendMessage(formatted);
            }
        }
    }

    public void shutdown() {
        try {
            done = true;
            pool.shutdown();
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    class ConnectionHandler implements Runnable {
        private final Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String username;
        private Room currentRoom;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                initializeStreams();
                authenticateUser();
                enterLobby();
            } catch (IOException e) {
                logout();
            }
        }

        private void initializeStreams() throws IOException {
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        }

        private void authenticateUser() throws IOException {
            while (true) {
                out.println("Enter command: /login <username> <password> or /register <username> <password>");
                String input = in.readLine();
                if (input == null) continue;

                String[] parts = input.trim().split("\\s+");
                if (parts.length != 3) {
                    out.println("Invalid command format. Try again.");
                    continue;
                }

                String command = parts[0];
                String usernameInput = parts[1];
                String passwordInput = parts[2];

                switch (command.toLowerCase()) {
                    case "/login":
                        if (AuthService.authenticate(usernameInput, passwordInput)) {
                            this.username = usernameInput;
                            synchronized (connections) {
                                connections.add(this);
                            }
                            out.println("Login successful. You are now in the lobby");
                            System.out.println(username + " logged in");
                            return;
                        } else {
                            out.println("Invalid credentials or user already logged in. Try again.");
                        }
                        break;

                    case "/register":
                        if (AuthService.register(usernameInput, passwordInput)) {
                            this.username = usernameInput;
                            synchronized (connections) {
                                connections.add(this);
                            }
                            out.println("Registration successful. You are now logged in.");
                            System.out.println(username + " registered and logged in");
                            return;
                        } else {
                            out.println("Username already exists. Try a different one.");
                        }
                        break;

                    default:
                        out.println("Unknown command. Use /login or /register.");
                }
            }
        }

        private void enterLobby() throws IOException {
            currentRoom = null;
            displayRooms();

            while (true) {
                String input = promptInput("Enter a command (e.g., /join <roomname> or /create <roomname>):");
                if (input == null) {
                    logout();
                    return;
                }

                input = input.trim();
                if (!handleCommand(input)) {
                    sendMessage("Invalid command. Use /join <roomname>, /create <roomname>, or /room list.");
                } else if (currentRoom != null) {
                    // Successfully joined a room, start chat
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
            while (true) {
                String message = in.readLine();
                if (message == null) {
                    logout();
                    return;
                }

                message = message.trim();
                if (message.isEmpty()) continue;

                if (message.startsWith("/")) {
                    if (!handleCommand(message)) {
                        sendMessage("Invalid command. Try again.");
                    }
                } else if (currentRoom != null) {
                    broadcast(Message.createUserMessage(username, message), currentRoom);

                    if (currentRoom.isAI()) {
                        currentRoom.addMessage(username + ": " + message);
                    }
                } else {
                    sendMessage("You are not in a room. This should not happen!");
                }
            }
        }

        private boolean handleCommand(String input) {
            String[] parts = input.split("\\s+", 2);
            String command = parts[0].toLowerCase();
            String argument = (parts.length > 1) ? parts[1].trim() : null;

            switch (command) {
                case "/quit":
                    logout();
                    return true;

                case "/room":
                    if (argument.equalsIgnoreCase("list")) {
                        displayRooms();
                        return true;
                    }
                    sendMessage("Usage: /room list");
                    return true;

                case "/join":
                    if (argument == null) {
                        sendMessage("Usage: /join <roomname>");
                        return true;
                    }

                    if (currentRoom == null) {
                        joinRoom(argument);
                    } else {
                        switchRoom(argument);
                    }
                    return true;


                case "/create":
                    if (argument == null) {
                        sendMessage("Usage: /create <roomname> [AI room : 0|1]");
                        return true;
                    }
                    createRoom(argument);
                    return true;

                case "/ai":
                    if (currentRoom == null || !currentRoom.isAI()) {
                        sendMessage("This is not an AI-powered room.");
                        return true;
                    }
                    if (argument == null) {
                        sendMessage("Usage: /ai <message>");
                        return true;
                    }
                    handleAIMessage(argument);
                    return true;

                default:
                    return false;
            }
        }

        private void handleAIMessage(String userQuery) {
            try {
                String context = String.join("\n", currentRoom.getMessageHistory());
                String prompt = "Chat history:\n" + context + "\n\nUser: " + userQuery;

                // Call LLM for response
                String llmResponse = LLMService.getLLMResponseContent(userQuery, prompt);

                // Store AI response in history
                currentRoom.addMessage("BOT: " + llmResponse);

                // Broadcast AI message
                broadcast(Message.createUserMessage("BOT", llmResponse), currentRoom);
            } catch (Exception e) {
                sendMessage("AI failed to respond.");
                e.printStackTrace();
            }
        }

        private boolean switchRoom(String roomName) {
            if (!RoomManager.isValidRoom(roomName)) {
                sendMessage("Invalid room name: " + roomName);
                return false;
            }

            leaveRoom();
            return joinRoom(roomName);
        }


        private boolean joinRoom(String roomName) {
            if (!RoomManager.isValidRoom(roomName)) {
                sendMessage("Room '" + roomName + "' does not exist. Use /create <roomname> to create it.");
                return false;
            }

            currentRoom = RoomManager.getRoom(roomName);
            currentRoom.addUser(username);
            sendMessage("You have joined room: " + roomName);
            broadcast(Message.createSystemMessage(username + " joined the room!"), currentRoom);
            System.out.println(username + " joined the room " + currentRoom.getName());
            return true;
        }

        private void leaveRoom() {
            if (currentRoom != null) {
                int userCount = currentRoom.getUserCount();
                currentRoom.removeUser(username);
                broadcast(Message.createSystemMessage(username + " left the room"), currentRoom);
                System.out.println(username + " left the room " + currentRoom.getName());

                if (userCount == 1 && !currentRoom.isSystem()) {
                    System.out.println("Room " + currentRoom.getName() + " is now empty and will be deleted.");
                    RoomManager.removeRoom(currentRoom.getName());
                }

                currentRoom = null;
            }
        }

        private boolean createRoom(String argument) {
            String[] args = argument.split("\\s+");
            String roomName = args[0].trim();
            boolean isAI = false;

            if (args.length > 1) {
                if (args[1].equals("1")) {
                    isAI = true;
                } else {
                    sendMessage("Invalid second argument. Use 1 to create an AI room.");
                    return false;
                }
            }
            if (RoomManager.isValidRoom(roomName)) {
                sendMessage("Room '" + roomName + "' already exists. Use /join <roomname> to enter.");
                return false;
            }

            RoomManager.createRoom(roomName, isAI);

            if (currentRoom == null) {
                return joinRoom(roomName);
            }
            return switchRoom(roomName);
        }

        private String promptInput(String prompt) throws IOException {
            sendMessage(prompt);
            return in.readLine();
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void logout() {
            try {
                if (currentRoom != null) {
                    //currentRoom.removeUser(username);
                    leaveRoom();
                    //broadcast(Message.createSystemMessage(username + " left the chat"), currentRoom);
                }

                if (username != null) {
                    synchronized (connections) {
                        connections.remove(this);
                    }
                    AuthService.logout(username);
                    //broadcast(Message.createSystemMessage(username + " left the chat"), currentRoom);
                }
                broadcast(Message.createSystemMessage(username + " left the chat"), currentRoom);
                System.out.println(username + " logged out");

                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void shutdown() {
            try{
                if (in != null) in.close();
                if (out != null) out.close();
                if (!client.isClosed()) client.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        public Room getCurrentRoom() {
            return currentRoom;
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}