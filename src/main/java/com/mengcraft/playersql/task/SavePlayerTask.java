package com.mengcraft.playersql.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.mengcraft.jdbc.ConnectionHandler;

public class SavePlayerTask implements Runnable {

	private final int quit;
	private final Map<UUID, String> map;

	@Override
	public void run() {
		try {
			Connection connection = ConnectionHandler.getConnection("playersql");
			PreparedStatement sql = connection.prepareStatement("UPDATE `PlayerData` SET `Data` = ?, `Online` = ?, `Last` = ? WHERE `Player` = ?;");
			sql.setLong(3, System.currentTimeMillis());
			sql.setInt(2, this.quit);
			for (Entry<UUID, String> entry : this.map.entrySet()) {
				sql.setString(4, entry.getKey().toString());
				sql.setString(1, entry.getValue());
				sql.addBatch();
			}
			sql.executeBatch();
			sql.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public SavePlayerTask(Map<UUID, String> map, int quit) {
		this.map = map;
		this.quit = quit;
	}

	public SavePlayerTask(UUID uuid, String data) {
		HashMap<UUID, String> empty = new HashMap<>();
		empty.put(uuid, data);
		this.map = empty;
		this.quit = 0;
	}

}
