package com.mengcraft.playersql;

import com.github.caoli5288.playersql.bungee.Constants;
import com.github.caoli5288.playersql.bungee.protocol.DataRequest;
import com.github.caoli5288.playersql.bungee.protocol.DataSupply;
import com.github.caoli5288.playersql.bungee.protocol.PeerReady;
import com.github.caoli5288.playersql.bungee.protocol.AbstractSqlPacket;
import com.github.caoli5288.playersql.bungee.protocol.ProtocolId;
import com.mengcraft.playersql.internal.GuidResolveService;
import com.mengcraft.playersql.lib.BiRegistry;
import com.mengcraft.playersql.lib.CustomInventory;
import com.mengcraft.playersql.task.FetchUserTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
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
    private final BiRegistry<Player, AbstractSqlPacket> registry = new BiRegistry<>();
    private final PluginMain main;
    private UserManager manager;
    private String group;

    public EventExecutor(PluginMain main) {
        manager = UserManager.INSTANCE;
        this.main = main;
        group = main.getConfig().getString("bungee.channel_group", "default");
        registry.register(ProtocolId.REQUEST, this::receiveRequest);
        registry.register(ProtocolId.CONTENTS, this::receiveContents);
    }

    private void receiveContents(Player player, AbstractSqlPacket packet) {
        main.debug("recv data_buf");
        DataSupply dataSupply = (DataSupply) packet;
        if (dataSupply.getBuf() == null || dataSupply.getBuf().length == 0 || !group.equals(dataSupply.getGroup())) {
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
                manager.pend(player, data);
                runAsync(() -> manager.updateDataLock(dataSupply.getId(), true));
            });
        }
    }

    private void receiveRequest(Player _p, AbstractSqlPacket packet) {
        DataRequest request = (DataRequest) packet;
        Player player = Bukkit.getPlayer(request.getId());
        if (player != null) {
            main.debug(String.format("receive data request for %s", player.getName()));
            handled.put(player.getUniqueId(), Lifecycle.DATA_SENT);
            player.kickPlayer(Constants.MAGIC_KICK);
        }
    }

    @EventHandler
    public void handle(InventoryCloseEvent e) {
        Inventory inventory = e.getInventory();
        if (CustomInventory.isInstance(inventory)) {
            CustomInventory.close(inventory);
        }
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!main.getConfig().getBoolean("bungee.mute")) {
            main.debug(String.format("PlayerJoin() -> send peer ready for %s", player.getName()));
            Utils.addChannel(player, Constants.PLUGIN_CHANNEL);
            PeerReady ready = new PeerReady();
            ready.setId(player.getUniqueId());
            player.sendPluginMessage(main, Constants.PLUGIN_CHANNEL, ready.encode());
        }

        manager.lockUser(player);
        UUID id = player.getUniqueId();
        Object pend = pending.remove(id);
        if (pend == null) {
            FetchUserTask task = new FetchUserTask(player);
            pending.put(id, task);
            task.runTaskTimerAsynchronously(main, Config.SYN_DELAY, Config.SYN_DELAY);
        } else if (pend instanceof PlayerData) {
            main.debug("process pending data_buf on join event");
            UUID guid = GuidResolveService.getService().getGuid(player);
            main.run(() -> {
                manager.pend(player, (PlayerData) pend);
                runAsync(() -> manager.updateDataLock(guid, true));
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
            manager.lockUser(player);
            PlayerData dat = manager.getUserData(player, true);
            pending.put(player.getUniqueId(), dat);
            supply.setBuf(PlayerDataHelper.encode(dat));
        }

        byte[] message = supply.encode();
        if (message.length > Messenger.MAX_MESSAGE_SIZE) {// Overflow?
            supply.setBuf(EMPTY_ARRAY);
            message = supply.encode();
        }

        player.sendPluginMessage(main, Constants.PLUGIN_CHANNEL, message);// BungeeCord received this before kicks
    }

    @EventHandler(priority = MONITOR)
    public void handle(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        Lifecycle lifecycle = handled.remove(id);
        if (manager.isNotLocked(id)) {
            manager.cancelTimerSaver(id);
            manager.lockUser(player);// Lock user if not in bungee enchant mode
            PlayerData data = (lifecycle == Lifecycle.DATA_SENT)
                    ? (PlayerData) pending.get(id)
                    : manager.getUserData(id, true);
            if (data == null) {
                main.run(() -> manager.unlockUser(player));// Err? unlock next tick
            } else {
                runAsync(() -> manager.saveUser(data, false)).thenRun(() -> main.run(() -> manager.unlockUser(player)));
            }
        } else {
        	UUID guid = GuidResolveService.getService().getGuid(player);
            runAsync(() -> manager.updateDataLock(guid, false)).thenRun(() -> main.run(() -> manager.unlockUser(player)));
        }
        pending.remove(id);
        LocalDataMgr.quit(player);
    }

    public void onPluginMessageReceived(String tag, Player p, byte[] input) {
        if (main.getConfig().getBoolean("bungee.mute") || !tag.equals(Constants.PLUGIN_CHANNEL)) {
            return;
        }

        AbstractSqlPacket ipk = AbstractSqlPacket.decode(input);
        registry.handle(ipk.getProtocol(), p, ipk);
    }

    /**
     * @deprecated meaningless flags
     */
    enum Lifecycle {

        INIT,
        DATA_SENT;
    }

}
