package com.mengcraft.playersql;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

/**
 * Created on 16-1-2.
 */
public class EventExecutor implements Listener {

    private final List<UUID> locked = new ArrayList<>();
    private PluginMain main;
    private UserManager userManager;

    @EventHandler
    public void handle(PlayerLoginEvent event) {
        this.locked.add(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        FetchUserTask task = new FetchUserTask();
        task.setUuid(event.getPlayer().getUniqueId());
        task.setExecutor(this);
        task.setTask(this.main.runTaskTimerAsynchronously(task, Config.SYN_DELAY));
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

}
