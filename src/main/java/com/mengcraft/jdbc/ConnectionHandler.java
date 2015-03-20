package com.mengcraft.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionHandler {

	public static final ConcurrentHashMap<String, ConnectionHandler> MAP = new ConcurrentHashMap<>();

	private final ConnectionFactory factory;
	private Connection connection;

	public ConnectionHandler(ConnectionFactory factory, String name) {
		this.factory = factory;
		MAP.put(name, this);
	}

	public static Connection getConnection(String name) {
		return MAP.get(name).getConnection();
	}

	public Connection getConnection() {
		if (connection == null || check() > 0) {
			try {
				connection = factory.create();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		return connection;
	}

	private int check() {
		try {
			return connection.isValid(1) ? 0 : 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
