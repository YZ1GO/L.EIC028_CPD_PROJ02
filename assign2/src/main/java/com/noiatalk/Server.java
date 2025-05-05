package com.noiatalk;

import com.noiatalk.models.Message;
import com.noiatalk.models.Room;
import com.noiatalk.services.ConfigLoader;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class Server implements Runnable {
    private final ThreadSafeConnectionList connections;
    private final ReentrantLock serverLock;
    private ServerSocket server;
    private volatile boolean running;

    public Server() {
        this.connections = new ThreadSafeConnectionList();
        this.serverLock = new ReentrantLock();
        this.running = false;
    }

    @Override
    public void run() {
        try {
            ConfigLoader.load("config.json");
            String portStr = ConfigLoader.get("SOCKET_PORT");
            int port = (portStr != null) ? Integer.parseInt(portStr) : 9999;

            serverLock.lock();
            try {
                server = new ServerSocket(port);
                running = true;
                System.out.println("Server started on port " + port);
            } finally {
                serverLock.unlock();
            }

            while (isRunning()) {
                try {
                    Socket client = server.accept();
                    ConnectionHandler handler = new ConnectionHandler(client, this);
                    Thread.startVirtualThread(handler);
                } catch (IOException e) {
                    if (isRunning()) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Server initialization failed: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    public void broadcast(Message message, Room room) {
        if (room == null) return;
        String formatted = message.getFormattedMessage();
        connections.forEach(handler -> {
            if (handler.getCurrentRoom() != null && handler.getCurrentRoom().equals(room)) {
                handler.sendMessage(formatted);
            }
        });
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void shutdown() {
        if (!running) return;

        running = false;
        serverLock.lock();
        try {
            if (server != null && !server.isClosed()) {
                try {
                    server.close();
                } catch (IOException e) {
                    System.err.println("Error closing server socket: " + e.getMessage());
                }
            }
        } finally {
            serverLock.unlock();
        }

        connections.forEach(ConnectionHandler::shutdown);
        connections.clear();
    }

    public void addConnection(ConnectionHandler connection) {
        connections.add(connection);
    }

    public void removeConnection(ConnectionHandler connection) {
        connections.remove(connection);
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}