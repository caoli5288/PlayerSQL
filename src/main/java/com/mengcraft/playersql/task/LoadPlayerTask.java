package com.mengcraft.playersql.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import com.google.gson.JsonParser;
import com.mengcraft.jdbc.ConnectionHandler;
import com.mengcraft.playersql.LoadTaskQueue;
import com.mengcraft.playersql.LoadedData;
import com.mengcraft.playersql.LoadedQueue;
import com.mengcraft.playersql.LockedPlayer;
import com.mengcraft.playersql.RetryHandler;

public class LoadPlayerTask implements Runnable {

	private final UUID uuid;

	private final Queue<LoadedData> queue;
	private final Queue<UUID> loads;
	private final List<UUID> locks;

	private final RetryHandler retry;

	@Override
	public void run() {
		try {
			Connection connection = ConnectionHandler.getConnection("playersql");
			PreparedStatement sql = connection.prepareStatement("SELECT `Data`, `Online` FROM `PlayerData` WHERE `Player` = ?;");
			sql.setString(1, this.uuid.toString());
			ResultSet result = sql.executeQuery();
			if (!result.next()) {
				// Create record for new player.
				PreparedStatement insert = connection.prepareStatement("INSERT INTO `PlayerData`(`Player`, `Online`) VALUES(?, 1);");
				insert.setString(1, this.uuid.toString());
				insert.executeUpdate();
				insert.close();
				connection.commit();
				// Unlock player.
				this.locks.remove(this.uuid);
			} else if (result.getInt(2) < 1) {
				// Data unlocked. lock and read it.
				PreparedStatement lock = connection.prepareStatement("UPDATE `PlayerData` SET `Online` = 1 WHERE `Player` = ?;");
				lock.setString(1, this.uuid.toString());
				lock.executeUpdate();
				connection.commit();
				lock.close();
				offer(result.getString(1));
			} else if (this.retry.check(this.uuid)) {
				// Data locked but reach max retry number.
				offer(result.getString(1));
			} else {
				this.loads.offer(this.uuid);
			}
			result.close();
			sql.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void offer(String data) {
		this.queue.offer(new LoadedData(this.uuid, new JsonParser().parse(data).getAsJsonArray()));
	}

	public LoadPlayerTask(UUID uuid) {
		this.uuid = uuid;
		this.queue = LoadedQueue.getDefault().getHandle();
		this.locks = LockedPlayer.getDefault().getHandle();
		this.loads = LoadTaskQueue.getManager().getHandle();
		this.retry = RetryHandler.getHandler();
	}

}
