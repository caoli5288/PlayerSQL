package com.mengcraft.playersql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.comphenix.protocol.utility.StreamSerializer;
import com.earth2me.essentials.craftbukkit.SetExpFix;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mengcraft.playersql.DBManager.PreparedAct;

/**
 * @author mengcraft.com
 */
public class TaskManaget {
	private final static TaskManaget MANAGET = new TaskManaget();
	private final Map<String, Integer> timerTask;

	public TaskManaget() {
		this.timerTask = new ConcurrentHashMap<String, Integer>();
	}

	public boolean isOnline(HumanEntity humanEntity) {
		return this.timerTask.containsKey(humanEntity.getName());
	}

	public void loadTask(Player player) {
		new Thread(new LoadTask(player)).start();
	}

	public void saveTask(Player player, boolean quit) {
		if (getTimerTask().containsKey(player.getName())) {
			Bukkit.getScheduler().cancelTask(getTimerTask().remove(player.getName()));
			new Thread(new SaveTask(player, quit)).start();
		}
	}

	public static TaskManaget getManaget() {
		return MANAGET;
	}

	public Map<String, Integer> getTimerTask() {
		return timerTask;
	}

	private class SaveTimer implements Runnable {
		private final String name;
		
		public SaveTimer(String name) {
			this.name = name;
		}
		@Override
		public void run() {
			new Thread(new SaveTask(Bukkit.getPlayerExact(this.name), false)).start();
		}
	}

	private class SaveTask implements Runnable {
		private final String uid;
		private final String name;
		private final String data;
		private final boolean quit;
	
		public SaveTask(Player player, boolean quit) {
			this.uid = player.getUniqueId().toString();
			this.name = player.getName();
			this.data = getPlayerData(player);
			this.quit = quit;
		}
	
		private String getPlayerData(Player player) {
			JsonArray array = new JsonArray();
			array.add(new JsonPrimitive(player.getHealth()));
			array.add(new JsonPrimitive(player.getFoodLevel()));
			array.add(new JsonPrimitive(SetExpFix.getTotalExperience(player)));
			array.add(stacksToArray(player.getInventory().getContents()));
			array.add(stacksToArray(player.getInventory().getArmorContents()));
			array.add(stacksToArray(player.getEnderChest().getContents()));
			array.add(effectsToArray(player.getActivePotionEffects()));
			return array.toString();
		}
	
		private JsonArray effectsToArray(Collection<PotionEffect> effects) {
			JsonArray array = new JsonArray();
			for (PotionEffect effect : effects) {
				JsonArray elements = new JsonArray();
				elements.add(new JsonPrimitive(effect.getType().getName()));
				elements.add(new JsonPrimitive(effect.getDuration()));
				elements.add(new JsonPrimitive(effect.getAmplifier()));
				elements.add(new JsonPrimitive(effect.isAmbient()));
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
			PreparedAct act = DBManager.getManager().getPreparedAct("UPDATE `PlayerSQL` SET `DATA` = ?, `ONLINE` = ? WHERE `NAME` = ?;");
			act.setString(1, this.data).setInt(2, this.quit ? 0 : 1).setString(3, PlayerSQL.get().getConfig().getBoolean("plugin.useuuid", true) ? this.uid : this.name);
			act.executeUpdate().close();
			Bukkit.getLogger().info("Save player " + this.name + " done!");
		}
	}

	private class LoadTask implements Runnable {
		private final String uid;
		private final String name;

		public LoadTask(Player player) {
			this.uid = player.getUniqueId().toString();
			this.name = player.getName();
		}

		@Override
		public void run() {
			action(0);
		}

		private void action(int i) {
			PreparedAct act = DBManager.getManager().getPreparedAct("SELECT * FROM `PlayerSQL` WHERE `NAME` = ? FOR UPDATE;");
			act.setString(1, PlayerSQL.get().getConfig().getBoolean("plugin.useuuid", true) ? this.uid : this.name).executeQuery();
			if (act.next()) {
				if (act.getInt(4) < 1) {
					updateLock();
					load(act.getString(3));
					getTimerTask().put(this.name, genTast());
				} else if (i < 10) {
					retry(i);
				} else {
					updateLock();
					load(act.getString(3));
					getTimerTask().put(this.name, genTast());
					Bukkit.getLogger().warning("Player " + this.name + "'s lock status ERROR");
				}
			} else {
				insertPlayer();
				getTimerTask().put(this.name, genTast());
			}
		}

		private void retry(int i) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			action(i + 1);
		}

		private int genTast() {
			return Bukkit.getScheduler().runTaskTimer(PlayerSQL.get(), new SaveTimer(this.name), 6000, 6000).getTaskId();
		}

		private void insertPlayer() {
			PreparedAct act = DBManager.getManager().getPreparedAct("INSERT INTO `PlayerSQL`(`NAME`, `ONLINE`) VALUES(?, 1);");
			act.setString(1, PlayerSQL.get().getConfig().getBoolean("plugin.useuuid", true) ? this.uid : this.name).executeUpdate().close();
		}

		private void load(String string) {
			JsonArray array = new JsonParser().parse(string).getAsJsonArray();
			Player player = Bukkit.getPlayerExact(this.name);
			if (player.isOnline()) {
				if (PlayerSQL.get().getConfig().getBoolean("sync.exp", true)) {
					SetExpFix.setTotalExperience(player, array.get(2).getAsInt());
				}
				if (PlayerSQL.get().getConfig().getBoolean("sync.potion", true)) {
					for (PotionEffect effect : player.getActivePotionEffects()) {
						player.removePotionEffect(effect.getType());
					}
					player.addPotionEffects(arrayToEffects(array.get(6).getAsJsonArray()));
				}
				if (PlayerSQL.get().getConfig().getBoolean("sync.health", true)) {
					try {
						player.setHealth(array.get(0).getAsDouble());
					} catch (IllegalArgumentException e) {
						player.setHealth(20);
					}
				}
				if (PlayerSQL.get().getConfig().getBoolean("sync.food", true)) {
					player.setFoodLevel(array.get(1).getAsInt());
				}
				if (PlayerSQL.get().getConfig().getBoolean("sync.inventory", true)) {
					player.getInventory().setContents(arrayToStacks(array.get(3).getAsJsonArray()));
					player.getInventory().setArmorContents(arrayToStacks(array.get(4).getAsJsonArray()));
					player.setItemOnCursor(new ItemStack(Material.AIR));
				}
				if (PlayerSQL.get().getConfig().getBoolean("sync.chest", true)) {
					player.getEnderChest().setContents(arrayToStacks(array.get(5).getAsJsonArray()));
				}
				Bukkit.getLogger().info("Load player " + player.getName() + " done!");
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

		private void updateLock() {
			PreparedAct act = DBManager.getManager().getPreparedAct("UPDATE `PlayerSQL` SET `ONLINE` = 1 WHERE `NAME` = ?;");
			act.setString(1, PlayerSQL.get().getConfig().getBoolean("plugin.useuuid", true) ? this.uid : this.name).executeUpdate().close();
		}

	}
}
