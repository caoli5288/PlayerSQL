package com.mengcraft.playersql;

import com.mengcraft.playersql.task.FetchUserTask;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.UUID;

import static org.bukkit.entity.EntityType.PLAYER;
import static org.bukkit.event.EventPriority.HIGHEST;
import static org.bukkit.event.EventPriority.LOWEST;
import static org.bukkit.event.EventPriority.MONITOR;

/**
 * Created on 16-1-2.
 */
public class EventExecutor implements Listener {

    private UserManager manager;
    private PluginMain main;

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
    public void handle(PlayerLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (Config.DEBUG) {
            main.log("Lock user " + uuid + " done!");
        }
        this.manager.lockUser(uuid);
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        val task = new FetchUserTask(main, event.getPlayer().getUniqueId());
        int delay = Config.SYN_DELAY;
        task.runTaskTimerAsynchronously(main, delay, delay);
    }

    @EventHandler(priority = MONITOR)
    public void handle(PlayerQuitEvent event) {
        val p = event.getPlayer().getUniqueId();
        if (manager.isNotLocked(p)) {
            manager.cancelTask(p);
            val i = manager.getUserData(p, true);
            main.runAsync(() -> manager.saveUser(i, false));
        } else {
            manager.unlockUser(p);
            main.runAsync(() -> manager.updateDataLock(p, false));
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

    private boolean isLocked(UUID uuid) {
        return manager.isLocked(uuid);
    }

    public void setManager(UserManager manager) {
        this.manager = manager;
    }

    public void setMain(PluginMain main) {
        this.main = main;
    }

}
