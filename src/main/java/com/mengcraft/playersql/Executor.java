package com.mengcraft.playersql;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mengcraft.playersql.SyncManager.State;
import com.mengcraft.playersql.api.PlayerPreSwitchServerEvent;

public class Executor implements Listener {

    private final SyncManager manager;
    private final DataCompound compond = DataCompound.DEFAULT;
    private final Main main;

    public Executor(Main main) {
        this.main = main;
        this.manager = main.manager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handle(PlayerPreSwitchServerEvent event) {
        if (!event.isCancelled()) {
            Player player = event.getPlayer();
            if (compond.state(player.getUniqueId()) == null) {
                compond.state(player.getUniqueId(), State.SWIT_WAIT);
                manager.saveAndSwitch(player, event.getTarget());
            }
        }
    }

    @EventHandler
    public void handle(PlayerLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        State state = compond.state(uuid);
        if (state != null && state != State.CONN_DONE) {
            event.setResult(Result.KICK_OTHER);
            event.setKickMessage(DataCompound.MESSAGE_KICK);
        } else if (event.getResult() == Result.ALLOWED) {
            compond.state(uuid, State.CONN_DONE);
        }
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        compond.state(event.getPlayer().getUniqueId(),
                State.JOIN_WAIT);
        if (Configs.MSG_ENABLE) event.getPlayer().sendMessage(
                Configs.MSG_LOADING);
        main.scheduler().runTaskLater(main,
                () -> manager.load(event.getPlayer()),
                Configs.SYN_DELY);
    }

    @EventHandler
    public void handle(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (compond.state(uuid) == null) {
            manager.save(player, true);
        }
    }

    @EventHandler
    public void handle(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        UUID uuid = event.getEntity().getUniqueId();
        if (compond.state(uuid) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handle(PlayerDropItemEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (compond.state(uuid) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handle(PlayerInteractEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (compond.state(uuid) != null) {
            event.setCancelled(true);
        }
    }

    public void register() {
        main.getServer().getPluginManager().registerEvents(this, main);
    }

}
