package com.mengcraft.playersql;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

public class Events implements Listener {

    private final SyncManager manager = SyncManager.DEFAULT;
    private final DataCompond compond = DataCompond.DEFAULT;
    private final Main main;

    public Events(Main main) {
        this.main = main;
    }

    @EventHandler
    public void handle(PlayerLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (compond.islocked(uuid)) {
            event.setResult(Result.KICK_OTHER);
            event.setKickMessage(DataCompond.MESSAGE_KICK);
        } else if (event.getResult() == Result.ALLOWED) {
            compond.lock(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void handle(final PlayerJoinEvent event) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                manager.load(event.getPlayer());
            }
        };
        main.scheduler().runTaskLater(main, task, 30);
    }

    @EventHandler
    public void handle(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!compond.islocked(uuid)) {
            manager.save(player, true);
        }
        compond.unlock(uuid);
    }

    @EventHandler
    public void handle(EntityDamageEvent event) {
        UUID uuid = event.getEntity().getUniqueId();
        if (compond.islocked(uuid)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handle(PlayerDropItemEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (compond.islocked(uuid)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handle(PlayerInteractEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (compond.islocked(uuid)) {
            event.setCancelled(true);
        }
    }

}
