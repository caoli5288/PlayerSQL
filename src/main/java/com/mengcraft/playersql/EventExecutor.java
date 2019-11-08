package com.mengcraft.playersql;

import com.mengcraft.playersql.lib.CustomInventory;
import com.mengcraft.playersql.peer.DataRequest;
import com.mengcraft.playersql.peer.DataSupply;
import com.mengcraft.playersql.peer.IPacket;
import com.mengcraft.playersql.peer.PeerReady;
import com.mengcraft.playersql.task.FetchUserTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
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
        registry.register(IPacket.Protocol.PEER_READY, this::receivePeer);
        registry.register(IPacket.Protocol.DATA_REQUEST, this::receiveRequest);
        registry.register(IPacket.Protocol.DATA_BUF, this::receiveContents);
    }

    private void receiveContents(Player ignore, IPacket packet) {
        main.debug("recv data_buf");
        DataSupply dataSupply = (DataSupply) packet;
        if (!group.equals(dataSupply.getGroup())) {
            return;
        }
        PlayerData data = PlayerDataHelper.decode(dataSupply.getBuf());
        BukkitRunnable pend = (BukkitRunnable) pending.remove(dataSupply.getId());
        if (pend == null) {
            main.debug("pending received data_buf");
            pending.put(dataSupply.getId(), data);
        } else {
            main.debug("process received data_buf");
            pend.cancel();
            main.run(() -> {
                manager.pend(data);
                runAsync(() -> manager.updateDataLock(dataSupply.getId(), true));
            });
        }
    }

    private void receiveRequest(Player ignore, IPacket packet) {
        main.debug("recv data_request");
        DataRequest data = (DataRequest) packet;
        Player player = Bukkit.getPlayer(data.getId());
        if (player == null) {
            return;
        }
        handled.put(player.getUniqueId(), Lifecycle.DATA_SENT);
        player.kickPlayer("playersql data request");
    }

    private void receivePeer(Player p, IPacket packet) {
        main.debug("recv peer_ready");
        PeerReady ready = (PeerReady) packet;// redirect it to enabled peer in bungeecord
        p.sendPluginMessage(main, IPacket.Protocol.TAG, ready.encode());
        handled.put(ready.getId(), Lifecycle.INIT);
    }

    @EventHandler
    public void handle(InventoryCloseEvent e) {
        Inventory inventory = e.getInventory();
        if (CustomInventory.isInstance(inventory)) {
            CustomInventory.close(inventory);
        }
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
        PlayerData pend = (PlayerData) pending.remove(id);
        if (pend == null) {
            FetchUserTask task = new FetchUserTask(main, event.getPlayer());
            pending.put(id, task);
            task.runTaskTimerAsynchronously(main, Config.SYN_DELAY, Config.SYN_DELAY);
        } else {
            main.debug("process pending data_buf on join event");
            main.run(() -> {
                manager.pend(pend);
                runAsync(() -> manager.updateDataLock(id, true));
            });
        }
    }

    @EventHandler
    public void handle(PlayerKickEvent e) {
        Player player = e.getPlayer();
        if (handled.get(player.getUniqueId()) != Lifecycle.DATA_SENT) {
            return;
        }

        DataSupply supply = new DataSupply();// So we magic send player data at kick event.
        supply.setId(player.getUniqueId());
        supply.setGroup(group);
        if (isLocked(player.getUniqueId())) {
            supply.setBuf(EMPTY_ARRAY);
        } else {
            manager.lockUser(player.getUniqueId());
            PlayerData dat = manager.getUserData(player, true);
            pending.put(player.getUniqueId(), dat);
            supply.setBuf(PlayerDataHelper.encode(dat));
        }

        byte[] message = supply.encode();
        if (message.length > Messenger.MAX_MESSAGE_SIZE) {// Overflow?
            supply.setBuf(EMPTY_ARRAY);
            message = supply.encode();
        }

        player.sendPluginMessage(main, IPacket.Protocol.TAG, message);// BungeeCord received this before kicks
    }

    @EventHandler(priority = MONITOR)
    public void handle(PlayerQuitEvent event) {
        /*
         * Magic quit processor first
         */
        UUID id = event.getPlayer().getUniqueId();
        if (manager.isNotLocked(id)) {
            manager.cancelTimerSaver(id);
            manager.lockUser(id);// Lock user if not in bungee enchant mode
            Lifecycle lifecycle = handled.get(id);
            PlayerData data = (lifecycle == Lifecycle.DATA_SENT)
                    ? (PlayerData) pending.get(id)
                    : manager.getUserData(id, true);
            if (data == null) {
                main.run(() -> manager.unlockUser(id));// Err? unlock next tick
            } else {
                runAsync(() -> manager.saveUser(data, false)).thenRun(() -> main.run(() -> manager.unlockUser(id)));
            }
        } else {
            runAsync(() -> manager.updateDataLock(id, false)).thenRun(() -> main.run(() -> manager.unlockUser(id)));
        }
        handled.remove(id);
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
