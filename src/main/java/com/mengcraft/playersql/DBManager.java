package com.mengcraft.playersql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author mengcraft.com
 */
public class DBManager {
	private final static DBManager MANAGER = new DBManager();
	private Connection connection = null;
	private String[] strings = null;

	private DBManager() {
	}

	public static DBManager getManager() {
		return MANAGER;
	}

	public PreparedAct getPreparedAct(String string) {
		return new PreparedAct(string);
	}

	public void executeUpdate(String string) {
		Statement statement = getStatement();
		try {
			statement.execute(string);
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean setConnection(String[] strings) {
		setStrings(strings);
		try {
			this.connection = DriverManager.getConnection(strings[0], strings[1], strings[2]);
		} catch (SQLException e) {
			return false;
		}
		return true;
	}

	private Statement getStatement() {
		try {
			Statement statement = getConnection().createStatement();
			return statement;
		} catch (SQLException e) {
			setConnection();
			return getStatement();
		}
	}

	private PreparedStatement getStatement(String string) {
		try {
			PreparedStatement statement = getConnection().prepareStatement(string);
			return statement;
		} catch (SQLException e) {
			setConnection();
			return getStatement(string);
		}
	}

	private Connection getConnection() {
		return connection;
	}

	private void setConnection() {
		String[] strings = getStrings();
		try {
			this.connection = DriverManager.getConnection(strings[0], strings[1], strings[2]);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private String[] getStrings() {
		return strings;
	}

	private void setStrings(String[] strings) {
		this.strings = strings;
	}

	public class PreparedAct {
		private final PreparedStatement act;
		private ResultSet resultSet = null;

		public PreparedAct(String string) {
			this.act = getStatement(string);
		}

		public void close() {
			try {
				if (getResultSet() != null) {
					getResultSet().close();
				}
				getAct().close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		public PreparedAct setString(int i, String string) {
			try {
				getAct().setString(i, string);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return this;
		}

		public PreparedAct setInt(int i, int j) {
			try {
				getAct().setInt(i, j);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return this;
		}

		public PreparedAct setLong(int i, long j) {
			try {
				getAct().setLong(i, j);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return this;
		}

		public int getInt(int i) {
			try {
				return getResultSet().getInt(i);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return 0;
		}

		public long getLong(int i) {
			try {
				return getResultSet().getLong(i);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return 0;
		}

		public String getString(int i) {
			try {
				return getResultSet().getString(i);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;
		}

		public PreparedAct executeUpdate() {
			try {
				getAct().executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return this;
		}

		public PreparedAct executeQuery() {
			try {
				if (this.resultSet != null) {
					this.resultSet.close();
				}
				setResultSet(getAct().executeQuery());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return this;
		}

		public boolean next() {
			try {
				return getResultSet().next();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return false;
		}

		private PreparedStatement getAct() {
			return act;
		}

		private ResultSet getResultSet() {
			return resultSet;
		}

		private void setResultSet(ResultSet resultSet) {
			this.resultSet = resultSet;
		}
	}
}
