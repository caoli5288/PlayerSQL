package com.mengcraft.playersql.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager
{
	private static final DBManager MANAGER = new DBManager();
	private Connection connection = null;
	private String[] strings = null;

	public static DBManager getManager()
	{
		return MANAGER;
	}

	public PreparedAct getPreparedAct(String string)
	{
		return new PreparedAct(string);
	}

	public void executeUpdate(String string)
	{
		Statement statement = getStatement();
		try
		{
			statement.execute(string);
			statement.close();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public boolean setConnection(String[] strings)
	{
		setStrings(strings);
		try
		{
			this.connection = DriverManager.getConnection(strings[0], strings[1], strings[2]);
		} catch (SQLException e)
		{
			return false;
		}
		return true;
	}

	private Statement getStatement()
	{
		try
		{
			return getConnection().createStatement();
		} catch (SQLException e)
		{
			setConnection();
		}
		return getStatement();
	}

	private PreparedStatement getStatement(String string)
	{
		try
		{
			return getConnection().prepareStatement(string);
		} catch (SQLException e)
		{
			setConnection();
		}
		return getStatement(string);
	}

	private Connection getConnection()
	{
		try
		{
			if (this.connection.isClosed()) {
				setConnection();
			}
		} catch (SQLException e)
		{
			setConnection();
		}
		return this.connection;
	}

	private void setConnection()
	{
		if (this.strings == null) { throw new NullPointerException("Not set connect infomation yet."); }
		try
		{
			this.connection = DriverManager.getConnection(this.strings[0], this.strings[1], this.strings[2]);
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	private void setStrings(String[] strings)
	{
		this.strings = strings;
	}

	public class PreparedAct
	{
		private final PreparedStatement act;
		private ResultSet resultSet = null;

		public PreparedAct(String string)
		{
			this.act = DBManager.getManager().getStatement(string);
		}

		public void close()
		{
			try
			{
				if (this.resultSet != null) {
					this.resultSet.close();
				}
				this.act.close();
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
		}

		public PreparedAct addBatch()
		{
			try
			{
				this.act.addBatch();
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
			return this;
		}

		public PreparedAct setString(int i, String string)
		{
			try
			{
				this.act.setString(i, string);
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
			return this;
		}

		public PreparedAct setInt(int i, int j)
		{
			try
			{
				this.act.setInt(i, j);
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
			return this;
		}

		public PreparedAct setLong(int i, long j)
		{
			try
			{
				this.act.setLong(i, j);
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
			return this;
		}

		public int getInt(int i)
		{
			try
			{
				return this.resultSet.getInt(i);
			} catch (SQLException e) {
			}
			return 0;
		}

		public long getLong(int i)
		{
			try
			{
				return this.resultSet.getLong(i);
			} catch (SQLException e) {
			}
			return 0L;
		}

		public String getString(int i)
		{
			try
			{
				return this.resultSet.getString(i);
			} catch (SQLException e) {
			}
			return null;
		}

		public PreparedAct excuteBatch()
		{
			try
			{
				this.act.executeBatch();
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
			return this;
		}

		public PreparedAct executeUpdate()
		{
			try
			{
				this.act.executeUpdate();
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
			return this;
		}

		public PreparedAct executeQuery()
		{
			try
			{
				if (this.resultSet != null) {
					this.resultSet.close();
				}
				this.resultSet = this.act.executeQuery();
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
			return this;
		}

		public boolean next()
		{
			if (this.resultSet == null) { return false; }
			try
			{
				return this.resultSet.next();
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
			return false;
		}
	}
}
