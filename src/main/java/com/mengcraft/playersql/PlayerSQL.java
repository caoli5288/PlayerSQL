package com.mengcraft.playersql;

import java.sql.Connection;
import java.sql.Statement;

import org.bukkit.plugin.java.JavaPlugin;

import com.mengcraft.jdbc.ConnectionFactory;
import com.mengcraft.jdbc.ConnectionHandler;
import com.mengcraft.playersql.task.CheckLoadedTask;
import com.mengcraft.playersql.task.CheckQueuedTask;
import com.mengcraft.playersql.task.TimerSaveTask;

public class PlayerSQL extends JavaPlugin {

	@Override
	public void onEnable() {
		saveDefaultConfig();
		saveConfig();
		ConnectionFactory factory = new ConnectionFactory(
				getConfig().getString("plugin.database"),
				getConfig().getString("plugin.username"),
				getConfig().getString("plugin.password"));
		ConnectionHandler handler = new ConnectionHandler("playersql", factory);
		try {
			Connection connection = handler.getConnection();
			String sql = "CREATE TABLE IF NOT EXISTS PlayerData("
					+ "`Id` int NOT NULL AUTO_INCREMENT, "
					+ "`Player` text NULL, "
					+ "`Data` text NULL, "
					+ "`Online` int NULL, "
					+ "`Last` bigint NULL, "
					+ "PRIMARY KEY(`Id`));";
			Statement action = connection.createStatement();
			action.executeUpdate(sql);
			action.close();
			getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
			getServer().getScheduler().runTaskTimer(this, new CheckLoadedTask(this), 1, 1);
			getServer().getScheduler().runTaskTimer(this, new CheckQueuedTask(), 5, 5);
			getServer().getScheduler().runTaskTimer(this, new TimerSaveTask(this), 6000, 6000);

		} catch (Exception e) {
			getLogger().warning("Unable to connect to database.");
			e.printStackTrace();
			getLogger().warning("Shutting down server...");
			setEnabled(false);
			getServer().shutdown();
		}
	}

	@Override
	public void onDisable() {
		try {
			TaskManager.getManager().runSaveAll(this, 0);
		} catch (Exception e) {
			getLogger().warning("Unable to connect to database.");
			e.printStackTrace();
		}
	}

}
