package com.noiatalk.utils;

import com.noiatalk.ConnectionHandler;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public class ThreadSafeConnectionList {
    private final ArrayList<ConnectionHandler> connections = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void add(ConnectionHandler handler) {
        lock.writeLock().lock();
        try {
            connections.add(handler);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void remove(ConnectionHandler handler) {
        lock.writeLock().lock();
        try {
            connections.remove(handler);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void forEach(Consumer<ConnectionHandler> action) {
        lock.readLock().lock();
        try {
            new ArrayList<>(connections).forEach(action);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            connections.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
