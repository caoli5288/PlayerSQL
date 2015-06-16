package com.mengcraft.playersql.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    public final static ConnectionManager DEFAULT = new ConnectionManager();

    private final Map<String, ConnectionHandler> map;

    public ConnectionHandler getHandler(String key, ConnectionFactory f) {
        ConnectionHandler handler = new ConnectionHandler(key, f);
        map.put(key, handler);
        return handler;
    }

    public Connection getConnection(String handle) throws SQLException {
        return map.get(handle).getConnection();
    }

    public void release(String handle, Connection c) {
        map.get(handle).release(c);
    }

    public ConnectionHandler getHandler(String key) {
        ConnectionHandler handler = map.get(key);
        if (handler == null) {
            throw new NoSuchElementException();
        }
        return handler;
    }

    public ConnectionManager() {
        this.map = new ConcurrentHashMap<>();
    }

    public void shutdown() {
        for (ConnectionHandler handler : map.values()) {
            handler.shutdown();
        }
        map.clear();
    }
}
