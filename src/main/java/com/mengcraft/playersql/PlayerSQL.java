package com.mengcraft.playersql;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import com.mengcraft.playersql.util.DBManager;

/**
 * @author mengcraft.com
 */

public class PlayerSQL extends JavaPlugin {

	@Override
	public void onEnable() {
		saveDefaultConfig();
		
		String[] strings = { getConfig().getString("plugin.database"), getConfig().getString("plugin.username"), getConfig().getString("plugin.password") };
		if (DBManager.getManager().setConnection(strings)) {
			String table = "`Id` int NOT NULL AUTO_INCREMENT, `Player` text NULL, `Data` text NULL, `Online` int NULL, `Last` bigint NULL, PRIMARY KEY(`Id`)";
			String sql = "CREATE TABLE IF NOT EXISTS PlayerData(" + table + ");";
			DBManager.getManager().executeUpdate(sql);
			Bukkit.getPluginManager().registerEvents(new Events(), this);
			Bukkit.getScheduler().runTaskTimer(this, TaskManaget.getManaget().getSaveTask(), 6000, 6000);
			try {
				new Metrics(this).start();
			} catch (IOException e) {
				getLogger().warning("Can not link to mcstats.org!");
			}
		} else {
			getLogger().warning("Can not link to your database!");
			getLogger().warning("Plugin will disable!");
			setEnabled(false);
		}
		getLogger().info("Author: min梦梦");
		
	}

	@Override
	public void onDisable() {
		TaskManaget.getManaget().saveAllTask(true);
	}
}
