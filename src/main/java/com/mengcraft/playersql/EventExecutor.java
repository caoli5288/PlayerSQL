package com.mengcraft.playersql;

import com.mengcraft.playersql.task.DailySaveTask;
import com.mengcraft.playersql.task.FetchUserTask;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.bukkit.event.EventPriority.HIGHEST;

/**
 * Created on 16-1-2.
 */
public class EventExecutor implements Listener {

    private UserManager userManager;
    private PluginMain main;

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
        synchronized (task) {
            task.setUuid(event.getPlayer().getUniqueId());
            task.setExecutor(this);
            task.setTaskId(this.main.runTaskTimerAsynchronously(task, Config.SYN_DELAY).getTaskId());
        }
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
        }
    }

    @EventHandler(ignoreCancelled = true, priority = HIGHEST)
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
    public void handle(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.PLAYER && this.userManager.isUserLocked(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
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
