package com.mengcraft.playersql.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.avaje.ebeaninternal.server.lib.sql.DataSourcePool;
import com.mengcraft.playersql.common.DataManager;

public class SavePlayerTask implements Runnable {

	private final DataSourcePool pool = DataManager.getDefault().getHandle().getDataSource("default");
	private final int quit;
	private final Map<UUID, String> map;

	@Override
	public void run() {
		try {
			Connection connection = this.pool.getConnection();
			PreparedStatement sql = connection.prepareStatement("UPDATE `PlayerData` SET `Data` = ?, `Online` = ?, `Last` = ? WHERE `Player` = ?;");
			sql.setLong(3, System.currentTimeMillis());
			sql.setInt(2, this.quit);
			addBatch(sql);
			sql.executeBatch();
			sql.close();
			connection.commit();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void addBatch(PreparedStatement sql) throws SQLException {
		for (Entry<UUID, String> entry : this.map.entrySet()) {
			sql.setString(4, entry.getKey().toString());
			sql.setString(1, entry.getValue());
			sql.addBatch();
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
