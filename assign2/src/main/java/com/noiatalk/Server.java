package com.noiatalk;

import com.noiatalk.models.Message;
import com.noiatalk.models.Room;
import com.noiatalk.services.AuthService;
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
            String portStr = System.getenv("SOCKET_PORT");
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
                out.println("Username: ");
                String usernameInput = in.readLine();
                out.println("Password: ");
                String passwordInput = in.readLine();

                if (AuthService.authenticate(usernameInput, passwordInput)) {
                    this.username = usernameInput;
                    synchronized (connections) {
                        connections.add(this);
                    }
                    out.println("Login successful. You are now in the lobby");
                    System.out.println(username + " logged in");
                    break;
                } else {
                    out.println("Invalid credentials or user already logged in. Try again.");
                }
            }
        }

        private void enterLobby() throws IOException {
            currentRoom = null;
            out.println("\n====== LOBBY ======");
            out.println("Available rooms:");
            displayRooms();

            String input;
            while (true) {
                input = promptInput("Enter a room name to join (or type '/quit' to exit):");
                if (input == null) {
                    logout();
                    return;
                }

                input = input.trim();
                if (input.equalsIgnoreCase("/quit")) {
                    logout();
                    return;
                }

                if (RoomManager.isValidRoom(input)) {
                    joinRoom(input);
                    handleChat();
                    break;
                } else {
                    sendMessage("Invalid room name. Try again.");
                }
            }
        }

        private void displayRooms() { sendMessage(RoomManager.getAvailableRoomsList()); }

        private void handleChat() throws IOException {
            String message;
            while (true) {
                message = in.readLine();
                if (message == null) {
                    logout();
                    return;
                }

                message = message.trim();
                if (message.isEmpty()) continue;

                if (handleRoomCommands(message)) {
                    continue;
                }

                if (currentRoom != null) {
                    broadcast(Message.createUserMessage(username, message), currentRoom);
                } else {
                    sendMessage("You are not in a room. This should not happen!");
                }
            }
        }

        private boolean handleRoomCommands(String message) {
            if (message.equalsIgnoreCase("/room list")) {
                displayRooms();
                return true;
            }

            if (message.toLowerCase().startsWith("/room ")) {
                String[] parts = message.split("\\s+", 2);
                if (parts.length < 2 || parts[1].trim().isEmpty()) {
                    sendMessage("Usage: /room <name>");
                    return true;
                }
                String newRoomName = parts[1].trim();

                if (RoomManager.isValidRoom(newRoomName)) {
                    leaveRoom();
                    joinRoom(newRoomName);
                } else {
                    sendMessage("Invalid room name: " + newRoomName);
                }
                return true;
            }
            return false;
        }

        private void joinRoom(String roomName) {
            currentRoom = RoomManager.getRoom(roomName);
            currentRoom.addUser(username);
            sendMessage("\nRoom: " + roomName);
            broadcast(Message.createSystemMessage(username + " joined the room"), currentRoom);
            System.out.println(username + " joined the room " + currentRoom.getName());
        }

        private void leaveRoom() {
            if (currentRoom != null) {
                currentRoom.removeUser(username);
                broadcast(Message.createSystemMessage(username + " left the room"), currentRoom);
                System.out.println(username + " left the room " + currentRoom.getName());
                currentRoom = null;
            }
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
                    currentRoom.removeUser(username);
                    broadcast(Message.createSystemMessage(username + " left the chat"), currentRoom);
                }

                if (username != null) {
                    synchronized (connections) {
                        connections.remove(this);
                    }
                    AuthService.logout(username);
                    System.out.println(username + " logged out");
                    broadcast(Message.createSystemMessage(username + " left the chat"), currentRoom);
                }

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