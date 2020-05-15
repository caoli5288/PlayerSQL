package com.mengcraft.playersql;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import static org.bukkit.event.EventPriority.HIGHEST;
import static org.bukkit.event.EventPriority.LOWEST;

/**
 * Created on 16-7-25.
 */
public class ExtendEventExecutor implements Listener {

    private final UserManager manager;

    public ExtendEventExecutor(UserManager manager) {
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true, priority = HIGHEST)
    public void handle(PlayerInteractAtEntityEvent event) {
        if (manager.isLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = LOWEST)
    public void pre(PlayerInteractAtEntityEvent event) {
        handle(event);
    }
}
