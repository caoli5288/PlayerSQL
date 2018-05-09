package com.mengcraft.playersql.locker;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import static com.mengcraft.playersql.UserManager.isLocked;
import static org.bukkit.entity.EntityType.PLAYER;
import static org.bukkit.event.EventPriority.HIGHEST;
import static org.bukkit.event.EventPriority.LOWEST;

public class EventLocker implements Listener {

    @EventHandler(priority = LOWEST)
    public void pre(AsyncPlayerChatEvent event) {
        handle(event);
    }

    @EventHandler(priority = LOWEST)
    public void pre(EntityDamageEvent event) {
        handle(event);
    }

    @EventHandler(priority = LOWEST)
    public void pre(InventoryClickEvent event) {
        handle(event);
    }

    @EventHandler(priority = LOWEST)
    public void pre(PlayerPickupItemEvent event) {
        handle(event);
    }

    @EventHandler(priority = LOWEST)
    public void pre(PlayerDropItemEvent event) {
        handle(event);
    }

    @EventHandler(priority = LOWEST)
    public void pre(PlayerInteractEntityEvent event) {
        handle(event);
    }

    @EventHandler(priority = LOWEST)
    public void pre(PlayerInteractEvent event) {
        handle(event);
    }

    @EventHandler(priority = LOWEST)
    public void pre(PlayerCommandPreprocessEvent event) {
        handle(event);
    }

    @EventHandler(priority = LOWEST)
    public void pre(PlayerToggleSneakEvent event) {
        handle(event);
    }

    @EventHandler(ignoreCancelled = true, priority = HIGHEST)
    public void handle(AsyncPlayerChatEvent event) {
        if (isLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = HIGHEST)
    public void handle(EntityDamageEvent event) {
        if (event.getEntityType() == PLAYER && isLocked(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = HIGHEST)
    public void handle(InventoryClickEvent event) {
        if (event.getWhoClicked().getType() == PLAYER && isLocked(event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
        }
    }
    @EventHandler
    public void handle(PlayerMoveEvent event) {
        if (isLocked(event.getPlayer().getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            from.setYaw(to.getYaw());
            from.setPitch(to.getPitch());
            event.setTo(from);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = HIGHEST)
    public void handle(PlayerPickupItemEvent event) {
        if (isLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = HIGHEST)
    public void handle(PlayerDropItemEvent event) {
        if (isLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = HIGHEST)
    public void handle(PlayerInteractEntityEvent event) {
        if (isLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = HIGHEST)
    public void handle(PlayerInteractEvent event) {
        if (isLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = HIGHEST)
    public void handle(PlayerCommandPreprocessEvent event) {
        if (isLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = HIGHEST)
    public void handle(PlayerToggleSneakEvent event) {
        if (isLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

}
