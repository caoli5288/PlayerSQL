package com.mengcraft.playersql;

import com.mengcraft.playersql.task.FetchUserTask;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import java.util.UUID;

import static org.bukkit.entity.EntityType.PLAYER;
import static org.bukkit.event.EventPriority.HIGHEST;
import static org.bukkit.event.EventPriority.LOWEST;

/**
 * Created on 16-1-2.
 */
public class EventExecutor implements Listener {

    private UserManager userManager;
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
        if (this.userManager.isUserLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = HIGHEST)
    public void handle(EntityDamageEvent event) {
        if (event.getEntityType() == PLAYER && this.userManager.isUserLocked(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = HIGHEST)
    public void handle(InventoryClickEvent event) {
        if (event.getWhoClicked().getType() == PLAYER && this.userManager.isUserLocked(event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
        }
    }

    @EventHandler
    public void handle(PlayerLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (Config.DEBUG) {
            main.logMessage("Lock user " + uuid + " done!");
        }
        this.userManager.lockUser(uuid);
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        FetchUserTask task = new FetchUserTask();
        task.setUuid(event.getPlayer().getUniqueId());
        task.setExecutor(this);
        task.setTaskId(this.main.runTaskTimerAsynchronously(task, Config.SYN_DELAY).getTaskId());
    }

    @EventHandler
    public void handle(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (userManager.isUserNotLocked(uuid)) {
            userManager.cancelTask(uuid);
            userManager.syncUser(uuid, true);
            main.runTaskAsynchronously(() -> {
                userManager.saveUser(uuid, false);
                userManager.cacheUser(uuid, null);
            });
        } else {
            userManager.unlockUser(uuid, false);
        }
    }

    @EventHandler
    public void handle(PlayerMoveEvent event) {
        if (this.userManager.isUserLocked(event.getPlayer().getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            from.setYaw(to.getYaw());
            from.setPitch(to.getPitch());
            event.setTo(from);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = HIGHEST)
    public void handle(PlayerPickupItemEvent event) {
        if (this.userManager.isUserLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = HIGHEST)
    public void handle(PlayerDropItemEvent event) {
        if (this.userManager.isUserLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = HIGHEST)
    public void handle(PlayerInteractEntityEvent event) {
        if (this.userManager.isUserLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = HIGHEST)
    public void handle(PlayerInteractEvent event) {
        if (this.userManager.isUserLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = HIGHEST)
    public void handle(PlayerCommandPreprocessEvent event) {
        if (this.userManager.isUserLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = HIGHEST)
    public void handle(PlayerToggleSneakEvent event) {
        if (this.userManager.isUserLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public UserManager getUserManager() {
        return this.userManager;
    }

    public void setMain(PluginMain main) {
        this.main = main;
    }

    public PluginMain getMain() {
        return this.main;
    }

    public void cancelTask(int taskId) {
        userManager.cancelTask(taskId);
    }

}
