package com.mengcraft.playersql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mengcraft.bukkit.reflect.util.PlayerUtil;
import com.mengcraft.bukkit.reflect.util.StackUtil;
import com.mengcraft.common.sql.DBManager;
import com.mengcraft.common.sql.DBManager.PreparedAct;

/**
 * @author mengcraft.com
 */
public class TaskManaget {
	private final static TaskManaget MANAGET = new TaskManaget();
	private final TimerTask timerTask = new TimerTask();
	private final ExecutorService pool = Executors.newCachedThreadPool();
	private final List<String> onlineList = Collections.synchronizedList(new ArrayList<String>());

	public static TaskManaget getManaget() {
		return MANAGET;
	}

	public boolean isOnline(String name) {
		return this.getOnlineList().contains(name);
	}

	public void loadTask(Player player) {
		this.pool.execute(new LoadTask(player));
	}

	public void saveAllTask(boolean isQuit) {
		if (Bukkit.getOnlinePlayers().length > 0) {
			this.pool.execute(new SaveTask(Bukkit.getOnlinePlayers(), isQuit));
		}
	}

	public void saveTask(Player player, boolean isQuit) {
		if (isOnline(player.getName())) {
			if (isQuit) {
				getOnlineList().remove(player.getName());
			}
			this.pool.execute(new SaveTask(player, isQuit));
		}
	}

	public List<String> getOnlineList() {
		return onlineList;
	}

	public TimerTask getSaveTask() {
		return timerTask;
	}

	private class TimerTask implements Runnable {
		@Override
		public void run() {
			TaskManaget.getManaget().saveAllTask(false);
		}
	}

	private class SaveTask implements Runnable {
		private final Map<String, String> PlayerMap = new HashMap<String, String>();
		private final boolean quit;

		public SaveTask(Player player, boolean quit) {
			String date = getPlayerData(player);
			if (Configure.USE_UUID) {
				this.PlayerMap.put(player.getUniqueId().toString(), date);
			} else {
				this.PlayerMap.put(player.getName(), date);
			}
			this.quit = quit;
		}

		public SaveTask(Player[] players, boolean isQuit) {
			List<Player> list = new ArrayList<>();
			for (Player player : players) {
				if (getOnlineList().contains(player.getName())) {
					list.add(player);
				}
			}
			for (Player player : list) {
				String date = getPlayerData(player);
				if (Configure.USE_UUID) {
					this.PlayerMap.put(player.getUniqueId().toString(), date);
				} else {
					this.PlayerMap.put(player.getName(), date);
				}
			}
			this.quit = isQuit;
		}

		@Override
		public void run() {
			PreparedAct act = DBManager.getManager().getPreparedAct("UPDATE `PlayerData` SET `Data` = ?, `Online` = ?, `Last` = ? WHERE `Player` = ?;");
			for (String name : this.PlayerMap.keySet()) {
				act.setString(1, this.PlayerMap.get(name));
				act.setInt(2, this.quit ? 0 : 1);
				act.setLong(3, System.currentTimeMillis());
				act.setString(4, name).addBatch();
			}
			act.excuteBatch().close();
		}

		private String getPlayerData(Player player) {
			JsonArray array = new JsonArray();
			array.add(new JsonPrimitive(player.getHealth()));
			array.add(new JsonPrimitive(player.getFoodLevel()));
			array.add(new JsonPrimitive(PlayerUtil.getUtil().getTotalExperience(player)));
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
			StackUtil util = new StackUtil();
			for (ItemStack content : contents) {
				if (content != null && content.getType() != Material.AIR) {
					array.add(json.toJsonTree(util.getString(content)));
				} else {
					array.add(json.toJsonTree(null));
				}
			}
			return array;
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

		/*
		 * 2014/12/14 add act.close()
		 */
		private void action(int i) {
			PreparedAct act = DBManager.getManager().getPreparedAct("SELECT `Data`, `Online` FROM `PlayerData` WHERE `Player` = ? FOR UPDATE");
			act.setString(1, Configure.USE_UUID ? this.uid : this.name).executeQuery();
			if (act.next()) {
				if (act.getInt(2) < 1) {
					updateLock();
					load(act.getString(1));
				} else if (i < 10) {
					retry(i);
				} else {
					updateLock();
					load(act.getString(1));
					Bukkit.getLogger().warning("Player " + this.name + "'s lock status ERROR");
				}
			} else {
				insertPlayer();
			}
			act.close();
		}

		private void retry(int i) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			action(i + 1);
		}

		private void insertPlayer() {
			PreparedAct act = DBManager.getManager().getPreparedAct("INSERT INTO `PlayerData`(`Player`, `Online`) VALUES(?, 1)");
			act.setString(1, Configure.USE_UUID ? this.uid : this.name).executeUpdate().close();
			getOnlineList().add(this.name);
		}

		private void load(String string) {
			JsonArray array = string != null ? new JsonParser().parse(string).getAsJsonArray() : null;
			Player player = Bukkit.getPlayerExact(this.name);
			if (player.isOnline() && array != null) {
				if (Configure.SYNC_EXP) {
					PlayerUtil.getUtil().setTotalExperience(player, array.get(2).getAsInt());
				}
				if (Configure.SYNC_POTION) {
					for (PotionEffect effect : player.getActivePotionEffects()) {
						player.removePotionEffect(effect.getType());
					}
					player.addPotionEffects(arrayToEffects(array.get(6).getAsJsonArray()));
				}
				if (Configure.SYNC_HEALTH) {
					try {
						player.setHealth(array.get(0).getAsDouble());
					} catch (IllegalArgumentException e) {
						player.setHealth(20);
					}
				}
				if (Configure.SYNC_FOOD) {
					player.setFoodLevel(array.get(1).getAsInt());
				}
				if (Configure.SYNC_INVENTORY) {
					player.getInventory().setContents(arrayToStacks(array.get(3).getAsJsonArray()));
					player.getInventory().setArmorContents(arrayToStacks(array.get(4).getAsJsonArray()));
					player.setItemOnCursor(new ItemStack(Material.AIR));
				}
				if (Configure.SYNC_CHEST) {
					player.getEnderChest().setContents(arrayToStacks(array.get(5).getAsJsonArray()));
				}
				Bukkit.getLogger().info("Load player " + player.getName() + " done!");
			}
			getOnlineList().add(this.name);
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
			StackUtil util = new StackUtil();
			for (JsonElement element : array) {
				if (element.isJsonNull()) {
					stackList.add(new ItemStack(Material.AIR));
				} else {
					stackList.add(stringToStack(util, element.getAsString()));
				}
			}
			return stackList.toArray(new ItemStack[array.size()]);
		}

		private ItemStack stringToStack(StackUtil util, String string) {
			try {
				return util.getItemStack(string);
			} catch (Exception e) {
				return new ItemStack(Material.AIR);
			}
		}

		private void updateLock() {
			PreparedAct act = DBManager.getManager().getPreparedAct("UPDATE `PlayerData` SET `Online` = 1 WHERE `Player` = ?;");
			act.setString(1, Configure.USE_UUID ? this.uid : this.name).executeUpdate().close();
		}
	}
}
