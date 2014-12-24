package com.mengcraft.playersql;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class Configure {
	public final static boolean SYNC_EXP;
	public final static boolean SYNC_POTION;
	public final static boolean SYNC_HEALTH;
	public final static boolean SYNC_FOOD;
	public final static boolean SYNC_INVENTORY;
	public final static boolean SYNC_CHEST;
	public final static boolean USE_UUID;

	static {
		FileConfiguration option = Bukkit.getPluginManager().getPlugin("PlayerSQL").getConfig();
		SYNC_CHEST = option.getBoolean("sync.chest", true);
		SYNC_EXP = option.getBoolean("sync.exp", true);
		SYNC_FOOD = option.getBoolean("sync.food", true);
		SYNC_HEALTH = option.getBoolean("sync.health", true);
		SYNC_INVENTORY = option.getBoolean("sync.inventory", true);
		SYNC_POTION = option.getBoolean("sync.potion", true);
		USE_UUID = option.getBoolean("plugin.useuuid");
	}
}
