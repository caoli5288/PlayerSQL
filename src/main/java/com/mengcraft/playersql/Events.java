package com.mengcraft.playersql;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import com.mengcraft.playersql.SyncManager.State;
import com.mengcraft.playersql.api.PlayerPreSwitchServerEvent;

public class Events implements Listener {

    private final SyncManager manager;
    private final DataCompound compound = DataCompound.DEFAULT;
    private final Main main;

    public Events(Main main) {
        this.main = main;
        this.manager = main.manager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void handle(PlayerPreSwitchServerEvent event) {
        if (!event.isCancelled()) {
            Player player = event.getPlayer();
            if (compound.state(player.getUniqueId()) == null) {
                compound.state(player.getUniqueId(), State.SWIT_WAIT);
                manager.saveAndSwitch(player, event.getTarget());
            }
        }
    }

    @EventHandler
    public void handle(PlayerLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        State state = compound.state(uuid);
        if (state != null && state != State.CONN_DONE) {
            event.setResult(Result.KICK_OTHER);
            event.setKickMessage(DataCompound.MESSAGE_KICK);
        } else if (event.getResult() == Result.ALLOWED) {
            compound.state(uuid, State.CONN_DONE);
        }
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        compound.state(event.getPlayer().getUniqueId(),
                State.JOIN_WAIT);
        if (Config.MSG_ENABLE) event.getPlayer().sendMessage(
                Config.MSG_LOADING);
        main.scheduler().runTaskLater(main,
                () -> manager.load(event.getPlayer()),
                Config.SYN_DELAY);
    }

    @EventHandler
    public void handle(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (compound.state(uuid) == null) {
            manager.save(player, true);
        }
    }

    @EventHandler
    public void handle(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        UUID uuid = event.getEntity().getUniqueId();
        if (compound.state(uuid) != null) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void handle(PlayerPickupItemEvent event) {
    	UUID uuid = event.getPlayer().getUniqueId();
        if (compound.state(uuid) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handle(PlayerDropItemEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (compound.state(uuid) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handle(PlayerInteractEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (compound.state(uuid) != null) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void handle(InventoryOpenEvent event) {
    	if (compound.state(event.getPlayer().getUniqueId()) != null) {
    		event.setCancelled(true);
    	}
    }

    @EventHandler
    public void handle(PlayerInteractEntityEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (compound.state(uuid) != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void handle(PlayerCommandPreprocessEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (compound.state(uuid) != null) {
            e.setCancelled(true);
        }
    }

    public void register() {
        main.getServer().getPluginManager().registerEvents(this, main);
    }

}
