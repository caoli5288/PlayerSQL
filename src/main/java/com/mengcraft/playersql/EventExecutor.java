package com.mengcraft.playersql;

import com.mengcraft.playersql.peer.DataBuf;
import com.mengcraft.playersql.peer.DataRequest;
import com.mengcraft.playersql.peer.IPacket;
import com.mengcraft.playersql.peer.PeerReady;
import com.mengcraft.playersql.task.FetchUserTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.entity.EntityType.PLAYER;
import static org.bukkit.event.EventPriority.*;

/**
 * Created on 16-1-2.
 */
public class EventExecutor implements Listener, PluginMessageListener {

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

    private final Map<UUID, Object> pending = new HashMap<>();

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        handled.put(id, Lifecycle.INIT);

        Object pend = this.pending.remove(id);
        if (pend == null) {
            FetchUserTask task = new FetchUserTask(main, event.getPlayer());
            pending.put(id, task);

            task.runTaskTimerAsynchronously(main, Config.SYN_DELAY, Config.SYN_DELAY);
        } else {
            main.debug("### process pending data_buf on join event");
            main.run(() -> {
                manager.pend((PlayerData) pend);
                PluginMain.runAsync(() -> manager.updateDataLock(id, true));
            });

        }
    }

    private final Map<UUID, Lifecycle> handled = new HashMap<>();

    @EventHandler(priority = MONITOR)
    public void handle(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        Lifecycle lifecycle = handled.remove(id);
        if (lifecycle == Lifecycle.DATA_SENT || manager.isNotLocked(id)) {
            manager.cancelTask(id);
            PlayerData dat = manager.getUserData(id, true);
            if (dat == null) {
                throw new IllegalStateException("error persist player's data while someone quit from server");
            }

            manager.lockUser(id);// maybe fix some issue
            main.runAsync(() -> manager.saveUser(dat, false)).thenRun(() -> main.run(() -> manager.unlockUser(id)));
        } else {
            manager.unlockUser(id);
            main.runAsync(() -> manager.updateDataLock(id, false));
        }

        pending.remove(id);
        LocalDataMgr.quit(event.getPlayer());
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

    private final BiFunctionRegistry<Player, IPacket, Void> registry = new BiFunctionRegistry<>();

    {
        registry.register(IPacket.Protocol.PEER_READY, (p, ipk) -> {
            main.debug("### recv peer_ready");
            PeerReady pk = (PeerReady) ipk;// redirect it to enabled peer in bungeecord
            p.sendPluginMessage(main, IPacket.Protocol.TAG, pk.encode());
            return null;
        });
        registry.register(IPacket.Protocol.DATA_REQUEST, (p, ipk) -> {
            main.debug("### recv data_request");
            DataRequest pk = (DataRequest) ipk;
            Player request = Bukkit.getPlayer(pk.getId());
            if (request == null) {
                return null;
            }
            DataBuf out = new DataBuf();
            out.setId(request.getUniqueId());
            if (manager.isLocked(request.getUniqueId())) {
                out.setBuf(new byte[0]);
            } else {
                manager.lockUser(request.getUniqueId());
                PlayerData dat = manager.getUserData(request, true);
                handled.put(request.getUniqueId(), Lifecycle.DATA_SENT);
                out.setBuf(PlayerDataHelper.encode(dat));
            }
            request.sendPluginMessage(main, IPacket.Protocol.TAG, out.encode());// send data_buf by target player
            return null;
        });
        registry.register(IPacket.Protocol.DATA_BUF, (p, ipk) -> {
            main.debug("### recv data_buf");
            DataBuf pk = (DataBuf) ipk;
            PlayerData dat = PlayerDataHelper.decode(pk.getBuf());
            BukkitRunnable pend = (BukkitRunnable) pending.remove(pk.getId());
            if (pend == null) {
                main.debug("### pending received data_buf");
                pending.put(pk.getId(), dat);
            } else {
                main.debug("### process received data_buf");
                pend.cancel();
                main.run(() -> {
                    manager.pend(dat);
                    PluginMain.runAsync(() -> manager.updateDataLock(pk.getId(), true));
                });
            }
            return null;
        });
    }

    enum Lifecycle {

        INIT,
        DATA_SENT;
    }

    public void onPluginMessageReceived(String tag, Player p, byte[] input) {
        if (!tag.equals(IPacket.Protocol.TAG)) {
            return;
        }

        IPacket ipk = IPacket.decode(input);
        registry.handle(ipk.getProtocol(), p, ipk);
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
