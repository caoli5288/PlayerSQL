package com.mengcraft.playersql;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

import com.mengcraft.playersql.task.LoadPlayerTask;
import com.mengcraft.playersql.task.SavePlayerTask;
import com.mengcraft.playersql.util.FixedExp;
import com.mengcraft.playersql.util.ItemUtil;

public class TaskManager {

	private final static TaskManager MANAGER = new TaskManager();
	private final ExecutorService pool = Executors.newCachedThreadPool();
	private final ItemUtil util = ItemUtil.getUtil();

	public void runLoadTask(UUID uuid) {
		this.pool.execute(new LoadPlayerTask(uuid));
	}

	public void runSaveTask(Player player) {
		this.pool.execute(new SavePlayerTask(player.getUniqueId(), getData(player)));
	}

	public void runSaveAll(Plugin plugin, int quit) {
		Map<UUID, String> map = new HashMap<>();
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			map.put(player.getUniqueId(), getData(player));
		}
		this.pool.execute(new SavePlayerTask(map, quit));
	}

	public String getData(Player player) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		builder.append(player.getHealth()).append(",");
		builder.append(player.getFoodLevel()).append(",");
		builder.append(FixedExp.getExp(player)).append(",");
		ItemStack[] inventory = player.getInventory().getContents();
		ItemStack[] armors = player.getInventory().getArmorContents();
		ItemStack[] chest = player.getEnderChest().getContents();
		builder.append(getString(inventory)).append(",");
		builder.append(getString(armors)).append(",");
		builder.append(getString(chest)).append(",");
		Collection<PotionEffect> effects = player.getActivePotionEffects();
		builder.append(getString(effects));
		builder.append("]");
		return builder.toString();
	}

	private String getString(Collection<PotionEffect> effects) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for (Iterator<PotionEffect> it = effects.iterator(); it.hasNext();) {
			PotionEffect effect = it.next();
			builder.append("[");
			builder.append("\"");
			builder.append(effect.getType().getName()).append(",");
			builder.append(effect.getDuration()).append(",");
			builder.append(effect.getAmplifier()).append(",");
			builder.append(effect.isAmbient());
			builder.append("\"");
			builder.append("]");
			if (it.hasNext()) {
				builder.append(",");
			}
		}
		builder.append("]");
		return builder.toString();
	}

	private String getString(ItemStack[] stacks) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for (int i = 0; i < stacks.length; i++) {
			if (i > 0) {
				builder.append(",");
			}
			ItemStack stack = stacks[i];
			if (stack != null && stack.getType() != Material.AIR) {
				builder.append("\"");
				builder.append(this.util.getString(stack));
				builder.append("\"");
			} else {
				builder.append("null");
			}
		}
		builder.append("]");
		return builder.toString();
	}

	public static TaskManager getManager() {
		return MANAGER;
	}
}
