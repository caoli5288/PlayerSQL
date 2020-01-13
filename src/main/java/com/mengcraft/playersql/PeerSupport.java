package com.mengcraft.playersql;

import com.mengcraft.playersql.lib.Pair;
import com.mengcraft.playersql.peer.DataRequest;
import com.mengcraft.playersql.peer.DataSupply;
import com.mengcraft.playersql.peer.IPacket;
import com.mengcraft.playersql.peer.PeerReady;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PeerSupport extends Plugin implements Listener {

    private final BiRegistry<ProxiedPlayer, IPacket> registry = new BiRegistry<>();

    private final Set<UUID> peers = new HashSet<>();
    private final Map<UUID, Pair<ServerInfo, DataSupply>> values = new HashMap<>();

    @Override
    public void onEnable() {
        registry.register(IPacket.Protocol.PEER_READY, this::peerReady);
        registry.register(IPacket.Protocol.DATA_CONTENTS, this::dataContents);
        getProxy().registerChannel(IPacket.NAMESPACE);
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getConsole().sendMessage(ChatColor.GREEN + "PlayerSQLPeerSupport enabled! Donate me plz. https://www.paypal.me/2732000916/5");
    }

    private void dataContents(ProxiedPlayer player, IPacket packet) {
        DataSupply supply = (DataSupply) packet;
        if (values.containsKey(supply.getId())) {
            if (supply.getBuf() == null || supply.getBuf().length == 0) {
                values.remove(supply.getId());
            } else {
                Pair<ServerInfo, DataSupply> pair = values.get(supply.getId());
                pair.setValue(supply);
            }
        }
    }

    private void peerReady(ProxiedPlayer player, IPacket packet) {
        peers.add(((PeerReady) packet).getId());
    }

    @EventHandler
    public void handle(PluginMessageEvent event) {
        if (!event.getTag().equals(IPacket.NAMESPACE)) {
            return;
        }
        IPacket packet = IPacket.decode(event.getData());
        registry.handle(packet.getProtocol(), ((ProxiedPlayer) event.getReceiver()), packet);
    }

    @EventHandler(priority = Byte.MAX_VALUE)
    public void handle(ServerConnectEvent event) {
        if (event.isCancelled()) {
            return;
        }
        UUID id = event.getPlayer().getUniqueId();
        values.remove(id);// force remove dirty values
        if (peers.contains(id)) {
            values.put(id, new Pair<>(event.getTarget()));
            event.setCancelled(true);
            DataRequest request = new DataRequest();
            request.setId(id);
            event.getPlayer().getServer().sendData(IPacket.NAMESPACE, request.encode());
        }
    }

    @EventHandler
    public void handle(ServerKickEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        if (peers.remove(id) && values.containsKey(id)) {// maybe not peers
            Pair<ServerInfo, DataSupply> data = values.get(id);
            event.setCancelled(true);
            event.setCancelServer(data.getKey());
        }
    }

    @EventHandler
    public void handle(ServerConnectedEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        if (values.containsKey(id)) {
            Pair<ServerInfo, DataSupply> data = values.remove(id);
            if (data.getValue() != null) {
                Server sender = getSender(event.getServer());
                sender.sendData(IPacket.NAMESPACE, data.getValue().encode());
            }
        }
    }

    private Server getSender(Server server) {
        Collection<ProxiedPlayer> players = server.getInfo().getPlayers();
        if (players.isEmpty()) {
            return server;
        }
        return players.iterator().next().getServer();
    }

    @EventHandler
    public void handle(PlayerDisconnectEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        peers.remove(id);
        values.remove(id);
    }
}
