package com.mengcraft.playersql;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author mengcraft.com
 */
public class Events implements Listener {

	@EventHandler
	public void onDropItem(PlayerDropItemEvent event) {
		event.setCancelled(!TaskManaget.getManaget().isOnline(event.getPlayer().getName()));
	}

	@EventHandler
	public void onOpenInventory(InventoryOpenEvent event) {
		event.setCancelled(!TaskManaget.getManaget().isOnline(event.getPlayer().getName()));
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		TaskManaget.getManaget().loadTask(event.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		// System.out.println("PlayerSQL.Events.OnPlayerQuit.Fire");
		TaskManaget.getManaget().saveTask(event.getPlayer(), true);
	}
}
