package com.mengcraft.playersql;

import com.mengcraft.playersql.lib.Pair;
import com.mengcraft.playersql.peer.DataSupply;
import com.mengcraft.playersql.peer.DataRequest;
import com.mengcraft.playersql.peer.IPacket;
import com.mengcraft.playersql.peer.PeerReady;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.*;

public class PeerSupport extends Plugin implements Listener {

    private final BiRegistry<ProxiedPlayer, IPacket> registry = new BiRegistry<>();

    private final Set<UUID> handled = new HashSet<>();
    private final Map<UUID, Pair<ServerInfo, DataSupply>> pending = new HashMap<>();

    @Override
    public void onEnable() {
        registry.register(IPacket.Protocol.PEER_READY, (p, ipk) -> {
            PeerReady pk = (PeerReady) ipk;
            handled.add(pk.getId());
            Pair<ServerInfo, DataSupply> pair = pending.remove(pk.getId());
            if (pair == null || pair.getValue() == null) {
                return;
            }
            p.getServer().sendData(IPacket.Protocol.TAG, pair.getValue().encode());
        });
        registry.register(IPacket.Protocol.DATA_BUF, (p, packet) -> {
            DataSupply supply = (DataSupply) packet;
            Pair<ServerInfo, DataSupply> pair = pending.get(supply.getId());
            pair.setValue(supply);
            handled.remove(supply.getId());
        });
        getProxy().registerChannel(IPacket.Protocol.TAG);
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getConsole().sendMessage(ChatColor.GREEN + "PlayerSQLPeerSupport enabled! Donate me plz. https://www.paypal.me/2732000916/5");
    }

    @EventHandler
    public void handle(PluginMessageEvent event) {
        if (!event.getTag().equals(IPacket.Protocol.TAG)) {
            return;
        }
        IPacket ipk = IPacket.decode(event.getData());
        registry.handle(ipk.getProtocol(), ((ProxiedPlayer) event.getReceiver()), ipk);
    }

    @EventHandler(priority = Byte.MAX_VALUE)
    public void handle(ServerConnectEvent event) {
        if (event.isCancelled() || !handled.contains(event.getPlayer().getUniqueId())) {
            return;
        }

        if (event.getTarget().equals(event.getPlayer().getServer().getInfo())) {// ?
            return;
        }

        event.setCancelled(true);

        pending.put(event.getPlayer().getUniqueId(), new Pair<>(event.getTarget()));

        DataRequest pk = new DataRequest();
        pk.setId(event.getPlayer().getUniqueId());
        event.getPlayer().getServer().sendData(IPacket.Protocol.TAG, pk.encode());
    }

    @EventHandler
    public void handle(ServerSwitchEvent event) {
        PeerReady pk = new PeerReady();
        pk.setId(event.getPlayer().getUniqueId());
        event.getPlayer().getServer().sendData(IPacket.Protocol.TAG, pk.encode());
    }

    @EventHandler
    public void handle(ServerKickEvent event) {
        ProxiedPlayer player = event.getPlayer();
        UUID id = player.getUniqueId();
        if (!pending.containsKey(id)) {
            return;
        }

        Pair<ServerInfo, DataSupply> pair = pending.get(id);
        event.setCancelled(true);
        event.setCancelServer(pair.getKey());
    }

    @EventHandler
    public void handle(PlayerDisconnectEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        handled.remove(id);
        pending.remove(id);
    }
}
