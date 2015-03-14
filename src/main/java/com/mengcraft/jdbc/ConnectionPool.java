package com.mengcraft.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class ConnectionPool {

	private final ConnectionFactory factory;
	private final BlockingQueue<Connection> pool;

	public ConnectionPool(ConnectionFactory factory) {
		this.factory = factory;
		this.pool = new LinkedBlockingDeque<>();
	}

	public Connection get() {
		Connection connection = pool.poll();
		if (connection == null) {
			connection = create();
		} else if (isClose(connection)) {
			connection = get();
		}
		return connection;
	}

	/**
	 * @see Connection#isClosed()
	 * @param connection
	 * @return
	 */
	private boolean isClose(Connection connection) {
		boolean b = true;
		try {
			b = connection.isClosed();
		} catch (SQLException e) {
			// Do nothings
		}
		return b;
	}

	private Connection create() {
		Connection connection = null;
		try {
			connection = factory.create();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return connection;
	}

	public void release(Connection connection) {
		pool.offer(connection);
	}

	public void shutdown() {
		for (Connection connection : pool) {
			close(connection);
		}
	}

	private void close(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				// Do nothings
			}
		}
	}

}
