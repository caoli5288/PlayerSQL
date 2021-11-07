package com.mengcraft.playersql;

import com.github.caoli5288.playersql.bungee.Constants;
import com.github.caoli5288.playersql.bungee.protocol.AbstractSqlPacket;
import com.github.caoli5288.playersql.bungee.protocol.DataRequest;
import com.github.caoli5288.playersql.bungee.protocol.DataSupply;
import com.github.caoli5288.playersql.bungee.protocol.PeerReady;
import com.github.caoli5288.playersql.bungee.protocol.ProtocolId;
import com.google.common.collect.Maps;
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

import java.util.Map;
import java.util.UUID;

import static com.mengcraft.playersql.PluginMain.runAsync;
import static com.mengcraft.playersql.UserManager.isLocked;
import static org.bukkit.event.EventPriority.MONITOR;

/**
 * Created on 16-1-2.
 */
public class EventExecutor implements Listener, PluginMessageListener {

    private final Map<UUID, UserState> states = Maps.newHashMap();
    private final BiRegistry<Player, AbstractSqlPacket> registry = new BiRegistry<>();
    private final PluginMain main;
    private final String group;
    private final UserManager manager = UserManager.INSTANCE;

    public EventExecutor(PluginMain main) {
        this.main = main;
        group = main.getConfig().getString("bungee.channel_group", "default");
        registry.register(ProtocolId.REQUEST, this::onConnect);
        registry.register(ProtocolId.CONTENTS, this::onContents);
    }

    private UserState ofState(UUID id) {
        return states.computeIfAbsent(id, uuid -> new UserState());
    }

    private void onContents(Player player, AbstractSqlPacket packet) {//
        main.debug(String.format("onContents(id=%s)", player.getUniqueId()));
        DataSupply contents = (DataSupply) packet;
        if (contents.getBuf() == null || contents.getBuf().length == 0 || !group.equals(contents.getGroup())) {
            return;
        }
        UserState state = ofState(contents.getId());
        PlayerData data = PlayerDataHelper.decode(contents.getBuf());
        if (state.getFetchTask() == null) {
            main.debug("pending received data_buf");
            state.setPlayerData(data);
        } else {
            main.debug("process received data_buf");
            state.getFetchTask().cancel();
            main.run(() -> {
                manager.pend(player, data);
                runAsync(() -> manager.updateDataLock(contents.getId(), true));
            });
        }
    }

    private void onConnect(Player __, AbstractSqlPacket packet) {
        DataRequest request = (DataRequest) packet;
        Player player = Bukkit.getPlayer(request.getId());
        if (player != null) {
            main.debug(String.format("receive data request for %s", player.getName()));
            ofState(request.getId()).setKicking(true);
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

        if (main.getConfig().getBoolean("bungee.enable")) {
            main.debug(String.format("PlayerJoin() -> send peer ready for %s", player.getName()));
            Utils.addChannel(player, Constants.PLUGIN_CHANNEL);
            PeerReady ready = new PeerReady();
            ready.setId(player.getUniqueId());
            player.sendPluginMessage(main, Constants.PLUGIN_CHANNEL, ready.encode());
        }

        manager.lockUser(player);
        UUID id = player.getUniqueId();
        UserState state = ofState(id);
        if (state.getPlayerData() == null) {
            FetchUserTask task = new FetchUserTask(player);
            state.setFetchTask(task);
            task.runTaskTimerAsynchronously(main, Config.SYN_DELAY, Config.SYN_DELAY);
        } else {
            main.debug("process pending data_buf on join event");
            UUID guid = GuidResolveService.getService().getGuid(player);
            main.run(() -> {
                manager.pend(player, state.getPlayerData());
                runAsync(() -> manager.updateDataLock(guid, true));
            });
        }
    }

    @EventHandler
    public void handle(PlayerKickEvent e) {
        Player player = e.getPlayer();
        UserState state = ofState(player.getUniqueId());
        if (!state.isKicking()) {
            return;
        }

        DataSupply supply = new DataSupply();// So we magic send player data at kick event.
        supply.setId(player.getUniqueId());
        supply.setGroup(group);
        if (isLocked(player.getUniqueId())) {
            supply.setBuf(Constants.EMPTY_ARRAY);
        } else {
            manager.lockUser(player);
            PlayerData playerData = manager.getUserData(player, true);
            state.setPlayerData(playerData);
            supply.setBuf(PlayerDataHelper.encode(playerData));
        }

        byte[] message = supply.encode();
        if (message.length > Messenger.MAX_MESSAGE_SIZE) {// Overflow?
            supply.setBuf(Constants.EMPTY_ARRAY);
            message = supply.encode();
        }

        player.sendPluginMessage(main, Constants.PLUGIN_CHANNEL, message);// BungeeCord received this before kicks
    }

    @EventHandler(priority = MONITOR)
    public void handle(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        UserState state = states.get(id);
        if (manager.isNotLocked(id)) {
            manager.cancelTimerSaver(id);
            manager.lockUser(player);// Lock user if not in bungee enchant mode
            PlayerData data = (state != null && state.isKicking() && state.getPlayerData() != null)
                    ? state.getPlayerData()
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
        // leaks check
        if (states.size() > 64 && states.size() > Bukkit.getMaxPlayers()) {
            states.keySet()
                    .removeIf(it -> Bukkit.getPlayer(it) == null);
        }
    }

    public void onPluginMessageReceived(String tag, Player p, byte[] input) {
        if (main.getConfig().getBoolean("bungee.mute") || !tag.equals(Constants.PLUGIN_CHANNEL)) {
            return;
        }

        AbstractSqlPacket ipk = AbstractSqlPacket.decode(input);
        registry.handle(ipk.getProtocol(), p, ipk);
    }
}
