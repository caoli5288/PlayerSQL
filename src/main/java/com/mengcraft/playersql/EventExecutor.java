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

import static org.bukkit.event.EventPriority.LOWEST;

/**
 * Created on 16-1-2.
 */
public class EventExecutor implements Listener {

    private final Map<UUID, BukkitTask> taskMap = new ConcurrentHashMap<>();

    private UserManager userManager;
    private PluginMain main;

    @EventHandler
    public void handle(PlayerLoginEvent event) {
        this.userManager.lockUser(event.getPlayer().getUniqueId());
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
        if (!this.userManager.isUserLocked(event.getPlayer().getUniqueId())) {
            User user = this.userManager.getUser(event.getPlayer().getUniqueId());
            this.taskMap.remove(event.getPlayer().getUniqueId()).cancel();
            this.userManager.syncUser(user);
            this.main.runTaskAsynchronously(() -> {
                this.userManager.saveUser(user, false);
            });
        }
    }

    @EventHandler(ignoreCancelled = true, priority = LOWEST)
    public void handle(PlayerMoveEvent event) {
        if (this.userManager.isUserLocked(event.getPlayer().getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            from.setYaw(to.getYaw());
            from.setPitch(to.getPitch());
            event.setTo(from);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = LOWEST)
    public void handle(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.PLAYER && this.userManager.isUserLocked(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = LOWEST)
    public void handle(PlayerPickupItemEvent event) {
        if (this.userManager.isUserLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = LOWEST)
    public void handle(PlayerDropItemEvent event) {
        if (this.userManager.isUserLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = LOWEST)
    public void handle(PlayerInteractEntityEvent event) {
        if (this.userManager.isUserLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = LOWEST)
    public void handle(PlayerInteractEvent event) {
        if (this.userManager.isUserLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = LOWEST)
    public void handle(PlayerCommandPreprocessEvent event) {
        if (this.userManager.isUserLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    public void cancelTask(int i) {
        this.main.getServer().getScheduler().cancelTask(i);
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

    public void createTask(UUID uuid) {
        if (Config.DEBUG) {
            this.main.logMessage("Scheduling daily save task for user " + uuid + '.');
        }
        DailySaveTask saveTask = new DailySaveTask();
        BukkitTask task = this.main.runTaskTimer(saveTask, 6000);
        synchronized (saveTask) {
            saveTask.setUuid(uuid);
            saveTask.setExecutor(this);
            saveTask.setTaskId(task.getTaskId());
        }
        BukkitTask old = this.taskMap.put(uuid, task);
        if (old != null) {
            if (Config.DEBUG) {
                this.main.logException(new PluginException("Already schedule task for user " + uuid + '!'));
            }
            old.cancel();
        }
    }

}
