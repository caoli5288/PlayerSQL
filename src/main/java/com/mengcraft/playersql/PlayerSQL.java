package com.mengcraft.playersql;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

/**
 * @author mengcraft.com
 */

public class PlayerSQL extends JavaPlugin {

	private static Plugin plugin;

	@Override
	public void onLoad() {
		saveDefaultConfig();
		setPlugin(this);
	}

	@Override
	public void onEnable() {
		String[] strings = { getConfig().getString("plugin.database"), getConfig().getString("plugin.username"), getConfig().getString("plugin.password") };
		if (DBManager.getManager().setConnection(strings)) {
			String table = "`ID` int NOT NULL AUTO_INCREMENT, `NAME` text NOT NULL, `DATA` text NULL, `ONLINE` int NULL, PRIMARY KEY(`ID`)";
			String sql = "CREATE TABLE IF NOT EXISTS PlayerSQL(" + table + ");";
			DBManager.getManager().executeUpdate(sql);
			Bukkit.getPluginManager().registerEvents(new Events(), this);
			try {
				new Metrics(this).start();
			} catch (IOException e) {
				getLogger().warning("Can NOT link to mcstats.org!");
			}
		} else {
			getLogger().warning("Can NOT link to your database!");
			getLogger().warning("Plugin will disable!");
			setEnabled(false);
		}
		getLogger().info("Author: min梦梦");
	}

	@Override
	public void onDisable() {
		for (Player player : getServer().getOnlinePlayers()) {
			TaskManaget.getManaget().saveTask(player, true);
		}
	}

	public static Plugin get() {
		return plugin;
	}

	private static void setPlugin(Plugin plugin) {
		PlayerSQL.plugin = plugin;
	}
}
