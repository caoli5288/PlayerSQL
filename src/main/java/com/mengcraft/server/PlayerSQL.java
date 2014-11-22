package com.mengcraft.server;

import com.comphenix.protocol.utility.StreamSerializer;
import com.earth2me.essentials.craftbukkit.SetExpFix;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.mcstats.Metrics;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GPLv2 license.
 */

public class PlayerSQL extends JavaPlugin {

	private Plugin plugin;
	private Connection connection;
	private boolean uuid;

	@Override
	public void onLoad() {
		saveDefaultConfig();
		this.plugin = this;
	}

	@Override
	public void onEnable() {
		setDatabase();
		setDataTable();
		getServer().getPluginManager().registerEvents(new Events(), this);

		String[] version = getServer().getBukkitVersion().split("-")[0].split("\\.");
		this.uuid = Integer.parseInt(version[1]) > 7
				|| (Integer.parseInt(version[1]) > 6
				&& Integer.parseInt(version[2]) > 5);
		getLogger().info("Author: min梦梦");
		getLogger().info("插件作者: min梦梦");
		try {
			new Metrics(this).start();
		} catch (IOException e) {
			getLogger().warning("Failed to connect to Metrics server!");
		}
	}

	@Override
	public void onDisable() {
		for (Player player : getServer().getOnlinePlayers()) {
			new Thread(new SavePlayerTask(player, true)).start();
		}
		getLogger().info("Author: min梦梦");
		getLogger().info("插件作者: min梦梦");
	}

	private void setDatabase() {
		try {
			String database = getConfig().getString("plugin.database");
			String username = getConfig().getString("plugin.username");
			String password = getConfig().getString("plugin.password");
			this.connection = DriverManager.getConnection(database, username, password);
		} catch (SQLException e) {
			getLogger().warning("Can not link to database server!");
			getServer().getPluginManager().disablePlugin(this);
		}

	}

	private void setDataTable() {
		try {
			Statement create = this.connection.createStatement();
			create.execute("CREATE TABLE IF NOT EXISTS PlayerSQL("
					+ "ID int NOT NULL AUTO_INCREMENT, "
					+ "NAME text NOT NULL, "
					+ "DATA text NULL, "
					+ "ONLINE int NULL, "
					+ "PRIMARY KEY(ID)" +
					");"
					);
			create.close();
		} catch (SQLException e) {
			getLogger().warning("Can not create table!");
		}
	}

	private class Events implements Listener {
		private final Map<String, Integer> onlineMap;

		public Events() {
			this.onlineMap = new ConcurrentHashMap<String, Integer>();
		}

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		public void openInventory(InventoryOpenEvent event) {
			event.setCancelled(!getOnlineMap().containsKey(event.getPlayer().getName()));
		}

		@EventHandler(priority = EventPriority.MONITOR)
		public void onPlayerQuit(PlayerQuitEvent event) {
			if (getOnlineMap().containsKey(event.getPlayer().getName())) {
				new Thread(new SavePlayerTask(event.getPlayer(), true)).start();
				getServer().getScheduler().cancelTask(getOnlineMap().remove(event.getPlayer().getName()));
			}
		}

		@EventHandler(priority = EventPriority.LOWEST)
		public void playerJoinEvent(PlayerJoinEvent event) {
			new Thread(new LoadPlayerTask(event.getPlayer())).start();
		}

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		public void playerDropItemEvent(PlayerDropItemEvent event) {
			event.setCancelled(!getOnlineMap().containsKey(event.getPlayer().getName()));
		}

		public Map<String, Integer> getOnlineMap() {
			return onlineMap;
		}

		private class SavePlayerTimer implements Runnable {
			private final String name;

			public SavePlayerTimer(String name) {
				this.name = name;
			}

			@Override
			public void run() {
				new Thread(new SavePlayerTask(getServer().getPlayerExact(this.name), false)).start();
			}
		}

		private class LoadPlayerTask implements Runnable {
			private final String name;
			private final String uid;
			private int check;

			public LoadPlayerTask(Player player) {
				this.name = player.getName();
				this.uid = player.getUniqueId().toString();
				this.check = 0;
			}

			@Override
			public void run() {
				try {
					PreparedStatement select = connection.prepareStatement("SELECT * FROM `PlayerSQL` WHERE `NAME` = ? FOR UPDATE;");
					select.setString(1, uuid ? this.uid : this.name);
					ResultSet result = select.executeQuery();
					if (result.next()) {
						if (result.getInt(4) < 1) {
							loadPlayer(result.getString(3));
							lockPlayer(result.getString(2));
							int tast = Bukkit.getScheduler().runTaskTimer(plugin, new SavePlayerTimer(this.name), 6000, 6000).getTaskId();
							getOnlineMap().put(this.name, tast);
						} else {
							if (check < 10) {
								check = check + 1;
								Thread.sleep(100);
								run();
							} else {
								loadPlayer(result.getString(3));
								int tast = Bukkit.getScheduler().runTaskTimer(plugin, new SavePlayerTimer(this.name), 6000, 6000).getTaskId();
								getOnlineMap().put(this.name, tast);
								getLogger().warning("Player " + name + " 's lock status error!");
							}
						}
					} else {
						newPlayer(uuid ? this.uid : this.name);
						int tast = Bukkit.getScheduler().runTaskTimer(plugin, new SavePlayerTimer(this.name), 6000, 6000).getTaskId();
						getOnlineMap().put(this.name, tast);
					}
					result.close();
					select.close();
				} catch (SQLException e) {
					setDatabase();
					run();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			private void lockPlayer(String player) throws SQLException {
				PreparedStatement update = connection
						.prepareStatement("UPDATE `PlayerSQL` SET `ONLINE` = 1 WHERE `NAME` = ?;");
				update.setString(1, player);
				update.executeUpdate();
				update.close();
			}

			private void loadPlayer(String data) {
				JsonArray array = new JsonParser().parse(data).getAsJsonArray();
				Player player = getServer().getPlayerExact(this.name);
				if (player.isOnline()) {
					if (getConfig().getBoolean("sync.exp", true)) {
						SetExpFix.setTotalExperience(player, array.get(2).getAsInt());
					}
					if (getConfig().getBoolean("sync.potion", true)) {
						for (PotionEffect effect : player.getActivePotionEffects()) {
							player.removePotionEffect(effect.getType());
						}
						player.addPotionEffects(arrayToEffects(array.get(6).getAsJsonArray()));
					}
					if (getConfig().getBoolean("sync.health", true)) {
						try {
							player.setHealth(array.get(0).getAsDouble());
						} catch (IllegalArgumentException e) {
							player.setHealth(20);
						}
					}
					if (getConfig().getBoolean("sync.food", true)) {
						player.setFoodLevel(array.get(1).getAsInt());
					}
					if (getConfig().getBoolean("sync.inventory", true)) {
						player.getInventory().setContents(arrayToStacks(array.get(3).getAsJsonArray()));
						player.getInventory().setArmorContents(arrayToStacks(array.get(4).getAsJsonArray()));
					}
					if (getConfig().getBoolean("sync.chest", true)) {
						player.getEnderChest().setContents(arrayToStacks(array.get(5).getAsJsonArray()));
					}
					getLogger().info("Load player " + name + " done!");
				}
			}

			private Collection<PotionEffect> arrayToEffects(JsonArray effectArray) {
				List<PotionEffect> effectList = new ArrayList<PotionEffect>();
				for (JsonElement element : effectArray) {
					JsonArray array = element.getAsJsonArray();
					String i = array.get(0).getAsString();
					int j = array.get(1).getAsInt();
					effectList.add(new PotionEffect(PotionEffectType.getByName(i), j, array.get(2).getAsInt(), array.get(3).getAsBoolean()));
				}
				return effectList;
			}

			private ItemStack[] arrayToStacks(JsonArray array) {
				List<ItemStack> stackList = new ArrayList<ItemStack>();
				StreamSerializer serializer = StreamSerializer.getDefault();
				try {
					for (JsonElement element : array) {
						if (element.isJsonNull()) {
							stackList.add(new ItemStack(Material.AIR));
						} else {
							stackList.add(serializer.deserializeItemStack(element.getAsString()));
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return stackList.toArray(new ItemStack[array.size()]);
			}

			private void newPlayer(String newName) throws SQLException {
				PreparedStatement insert = connection.prepareStatement("INSERT INTO `PlayerSQL`(`NAME`, `ONLINE`) VALUES(?, 1);");
				insert.setString(1, newName);
				insert.executeUpdate();
				insert.close();
				getLogger().info("Player " + name + " join!");
			}
		}
	}

	private class SavePlayerTask implements Runnable {
		private final String name;
		private final String data;
		private final boolean quit;

		public SavePlayerTask(Player player, boolean quit) {
			this.name = uuid ? player.getUniqueId().toString() : player.getName();
			this.data = getPlayerData(player);
			this.quit = quit;
		}

		private String getPlayerData(Player player) {
			Gson json = new Gson();
			JsonArray array = new JsonArray();
			array.add(json.toJsonTree(player.getHealth()));
			array.add(json.toJsonTree(player.getFoodLevel()));
			array.add(json.toJsonTree(SetExpFix.getTotalExperience(player)));
			array.add(stacksToArray(player.getInventory().getContents()));
			array.add(stacksToArray(player.getInventory().getArmorContents()));
			array.add(stacksToArray(player.getEnderChest().getContents()));
			array.add(effectsToArray(player.getActivePotionEffects()));
			return array.toString();
		}

		private JsonArray effectsToArray(Collection<PotionEffect> effects) {
			Gson json = new Gson();
			JsonArray array = new JsonArray();
			for (PotionEffect effect : effects) {
				JsonArray elements = new JsonArray();
				elements.add(json.toJsonTree(effect.getType().getName()));
				elements.add(json.toJsonTree(effect.getDuration()));
				elements.add(json.toJsonTree(effect.getAmplifier()));
				elements.add(json.toJsonTree(effect.isAmbient()));
				array.add(elements);
			}
			return array;
		}

		private JsonArray stacksToArray(ItemStack[] contents) {
			Gson json = new Gson();
			JsonArray array = new JsonArray();
			StreamSerializer serializer = StreamSerializer.getDefault();
			try {
				for (ItemStack content : contents) {
					if (content != null && content.getType() != Material.AIR) {
						array.add(json.toJsonTree(serializer.serializeItemStack(content)));
					} else {
						array.add(json.toJsonTree(null));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return array;
		}

		@Override
		public void run() {
			try {
				PreparedStatement save = connection
						.prepareStatement("UPDATE `PlayerSQL` SET `DATA` = ?, `ONLINE` = ? WHERE `NAME` = ?;");
				save.setString(1, this.data);
				save.setString(3, this.name);
				save.setInt(2, this.quit ? 0 : 1);
				save.executeUpdate();
				save.close();
			} catch (SQLException e) {
				setDatabase();
				run();
			}
			getLogger().info("Save player " + name + " done!");
		}
	}
}
