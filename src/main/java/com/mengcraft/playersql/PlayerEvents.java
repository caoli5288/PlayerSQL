package com.mengcraft.playersql;

import java.util.List;
import java.util.Queue;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEvents implements Listener {

	private final Queue<UUID> tasks;
	private final List<UUID> lockeds;
	private final TaskManager manager;

	@EventHandler(priority = EventPriority.MONITOR)
	public void onLogin(PlayerLoginEvent event) {
		if (event.getResult() != Result.ALLOWED) {
			// DO NOTHING
		} else {
			this.lockeds.add(event.getPlayer().getUniqueId());
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if (this.lockeds.contains(event.getEntity().getUniqueId())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		if (this.lockeds.contains(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onView(InventoryOpenEvent event) {
		if (this.lockeds.contains(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		this.tasks.offer(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		if (!this.lockeds.contains(event.getPlayer().getUniqueId())) {
			this.manager.runSaveTask(event.getPlayer());
		}
	}

	public PlayerEvents() {
		this.lockeds = LockedPlayer.getDefault().getHandle();
		this.manager = TaskManager.getManager();
		this.tasks = LoadTaskQueue.getManager().getHandle();
	}

}
