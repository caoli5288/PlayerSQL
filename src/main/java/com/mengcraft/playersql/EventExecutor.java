package com.mengcraft.playersql;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created on 16-1-2.
 */
public class EventExecutor implements Listener {

    private UserManager userManager;
    private PluginMain main;

    @EventHandler
    public void handle(PlayerLoginEvent event) {
        LOCKED.add(event.getPlayer().getUniqueId());
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

    public void unlock(UUID uuid) {
        synchronized (LOCKED) {
            LOCKED.remove(uuid);
        }
    }

    public final static List<UUID> LOCKED = new ArrayList<>();

}
