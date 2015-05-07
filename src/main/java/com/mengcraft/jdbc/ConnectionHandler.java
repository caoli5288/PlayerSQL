package com.mengcraft.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionHandler {

    private final String name;
    private final ConnectionFactory factory;
    private final LinkedBlockingQueue<Connection> queue;

    protected ConnectionHandler(String name, ConnectionFactory factory) {
        this.name = name;
        this.factory = factory;
        this.queue = new LinkedBlockingQueue<>();
    }

    public String name() {
        return name;
    }

    public Connection getConnection() throws SQLException {
        Connection c = queue.poll();
        if (c == null) {
            c = factory.create();
        } else if (!check(c)) {
            return getConnection();
        }
        return c;
    }

    public void release(Connection c) {
        queue.offer(c);
    }

    private boolean check(Connection c) {
        try {
            return c.isValid(1);
        } catch (SQLException e) {
            // e.printStackTrace();
        }
        return false;
    }

    public void shutdown() {
        for (Connection connection : queue) {
            close(connection);
        }
    }

    private void close(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            // e.printStackTrace();
        }
    }

}
