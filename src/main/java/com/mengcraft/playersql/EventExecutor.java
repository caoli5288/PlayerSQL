package com.mengcraft.playersql;

import com.mengcraft.playersql.peer.DataRequest;
import com.mengcraft.playersql.peer.DataSupply;
import com.mengcraft.playersql.peer.IPacket;
import com.mengcraft.playersql.peer.PeerReady;
import com.mengcraft.playersql.task.FetchUserTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.mengcraft.playersql.PluginMain.runAsync;
import static com.mengcraft.playersql.UserManager.isLocked;
import static org.bukkit.event.EventPriority.MONITOR;

/**
 * Created on 16-1-2.
 */
public class EventExecutor implements Listener, PluginMessageListener {

    private static final byte[] EMPTY_ARRAY = new byte[0];
    private final Map<UUID, Lifecycle> handled = new HashMap<>();
    private final Map<UUID, Object> pending = new HashMap<>();
    private final BiRegistry<Player, IPacket> registry = new BiRegistry<>();
    private final PluginMain main;
    private UserManager manager;
    private String group;

    public EventExecutor(PluginMain main) {
        manager = UserManager.INSTANCE;
        this.main = main;
        group = main.getConfig().getString("bungee.channel_group", "default");
        registry.register(IPacket.Protocol.PEER_READY, (p, ipk) -> {
            main.debug("### recv peer_ready");
            PeerReady pk = (PeerReady) ipk;// redirect it to enabled peer in bungeecord
            p.sendPluginMessage(main, IPacket.Protocol.TAG, pk.encode());
        });
        registry.register(IPacket.Protocol.DATA_REQUEST, (p, ipk) -> {
            main.debug("### recv data_request");
            DataRequest pk = (DataRequest) ipk;
            Player request = Bukkit.getPlayer(pk.getId());
            if (request == null) {
                return;
            }
            DataSupply out = new DataSupply();
            out.setId(request.getUniqueId());
            out.setGroup(group);
            if (isLocked(request.getUniqueId())) {
                out.setBuf(EMPTY_ARRAY);
            } else {
                manager.lockUser(request.getUniqueId());
                PlayerData dat = manager.getUserData(request, true);
                handled.put(request.getUniqueId(), Lifecycle.DATA_SENT);
                pending.put(request.getUniqueId(), dat);
                out.setBuf(PlayerDataHelper.encode(dat));
            }
            byte[] message = out.encode();
            if (message.length > Messenger.MAX_MESSAGE_SIZE) {
                // overflow?
                out.setBuf(EMPTY_ARRAY);
                message = out.encode();
            }
            request.sendPluginMessage(main, IPacket.Protocol.TAG, message);// send data_buf by target player
        });
        registry.register(IPacket.Protocol.DATA_BUF, (p, ipk) -> {
            main.debug("### recv data_buf");
            DataSupply pk = (DataSupply) ipk;
            if (!group.equals(pk.getGroup())) {
                return;
            }
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
                    runAsync(() -> manager.updateDataLock(pk.getId(), true));
                });
            }
        });
    }

    @EventHandler
    public void handle(PlayerLoginEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        main.debug("Lock user " + id + " done!");
        this.manager.lockUser(id);
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        handled.put(id, Lifecycle.INIT);

        PlayerData pend = (PlayerData) pending.remove(id);
        if (pend == null) {
            FetchUserTask task = new FetchUserTask(main, event.getPlayer());
            pending.put(id, task);
            task.runTaskTimerAsynchronously(main, Config.SYN_DELAY, Config.SYN_DELAY);
        } else {
            main.debug("### process pending data_buf on join event");
            main.run(() -> {
                manager.pend(pend);
                runAsync(() -> manager.updateDataLock(id, true));
            });
        }
    }

    @EventHandler(priority = MONITOR)
    public void handle(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        if (handled.remove(id) == Lifecycle.DATA_SENT) {
            manager.cancelTask(id);
            PlayerData dat = (PlayerData) pending.get(id);
            if (dat == null) {
                main.run(() -> manager.unlockUser(id));// Err? unlock next tick
            } else {
                runAsync(() -> manager.saveUser(dat, false)).thenRun(() -> main.run(() -> manager.unlockUser(id)));
            }
        } else if (manager.isNotLocked(id)) {
            manager.cancelTask(id);
            manager.lockUser(id);// Lock user if not in bungee enchant mode
            PlayerData dat = manager.getUserData(id, true);
            if (dat == null) {
                main.run(() -> manager.unlockUser(id));// Err? unlock next tick
            } else {
                runAsync(() -> manager.saveUser(dat, false)).thenRun(() -> main.run(() -> manager.unlockUser(id)));
            }
        } else {
            runAsync(() -> manager.updateDataLock(id, false)).thenRun(() -> main.run(() -> manager.unlockUser(id)));
        }
        pending.remove(id);
        LocalDataMgr.quit(event.getPlayer());
    }

    public void onPluginMessageReceived(String tag, Player p, byte[] input) {
        if (!tag.equals(IPacket.Protocol.TAG)) {
            return;
        }

        IPacket ipk = IPacket.decode(input);
        registry.handle(ipk.getProtocol(), p, ipk);
    }

    enum Lifecycle {

        INIT,
        DATA_SENT;
    }

}
