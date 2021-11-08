package com.github.caoli5288.playersql.bungee;

import com.github.caoli5288.playersql.bungee.protocol.AbstractSqlPacket;
import com.github.caoli5288.playersql.bungee.protocol.DataRequest;
import com.github.caoli5288.playersql.bungee.protocol.DataSupply;
import com.github.caoli5288.playersql.bungee.protocol.ProtocolId;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;
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
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public class PlayerSqlBungee extends Plugin implements Listener {

    private final Map<ProtocolId, BiConsumer<ProxiedPlayer, AbstractSqlPacket>> handlers = Maps.newHashMap();
    private final Map<UUID, ConnectState> states = Maps.newConcurrentMap();

    @Override
    public void onEnable() {
        handlers.put(ProtocolId.READY, this::peerReady);
        handlers.put(ProtocolId.CONTENTS, this::dataContents);
        getProxy().registerChannel(Constants.PLUGIN_CHANNEL);
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getConsole().sendMessage(ChatColor.GREEN + "PlayerSqlBungee enabled! Donate me plz. https://www.paypal.me/2732000916/5");
    }

    private void dataContents(ProxiedPlayer player, AbstractSqlPacket packet) {
        DataSupply supply = (DataSupply) packet;
        if (states.containsKey(supply.getId())) {
            if (supply.getBuf() == null || supply.getBuf().length == 0) {
                states.remove(supply.getId());
            } else {
                ConnectState state = states.get(supply.getId());
                state.setContents(supply);
            }
        }
    }

    private void peerReady(ProxiedPlayer player, AbstractSqlPacket packet) {
        if (Constants.DEBUG) {
            getLogger().info("PEER_READY id=" + player.getUniqueId());
        }
        states.put(player.getUniqueId(), new ConnectState());
    }

    @EventHandler
    public void handle(PluginMessageEvent event) {
        if (!event.getTag().equals(Constants.PLUGIN_CHANNEL)) {
            return;
        }
        AbstractSqlPacket packet = AbstractSqlPacket.decode(event.getData());
        handlers.get(packet.getProtocol()).accept((ProxiedPlayer) event.getReceiver(), packet);
    }

    @EventHandler(priority = Byte.MAX_VALUE)
    public void handle(ServerConnectEvent event) {
        UserConnection conn = (UserConnection) event.getPlayer();
        UUID id = conn.getUniqueId();
        ConnectState state = states.get(id);
        if (state == null) {
            return;
        }
        if (state.getContents() == null) {// check contents null to compatible with some balancer
            state.setConnect(event.getTarget());
            event.setCancelled(true);
            DataRequest request = new DataRequest();
            request.setId(id);
            conn.getServer().sendData(Constants.PLUGIN_CHANNEL, request.encode());
            if (Constants.DEBUG) {
                getLogger().info(String.format("onConnect(id=%s) cancel event for contents", id));
            }

        } else if (event.getReason() == ServerConnectEvent.Reason.KICK_REDIRECT) {
            event.setCancelled(true);
            conn.setServerJoinQueue(Lists.newLinkedList(conn.getPendingConnection().getListener().getServerPriority()));
            conn.connect(state.getConnect(), (success, e) -> {
                if (!success) {
                    states.remove(id);
                }
                if (Constants.DEBUG) {
                    getLogger().info(String.format("connect(id=%s, to=%s) success=%s", id, state.getConnect(), success));
                }
            }, true);
            if (Constants.DEBUG) {
                getLogger().info(String.format("onConnect(id=%s) KICK_REDIRECT to %s", id, state.getConnect()));
            }
        }
    }

    @EventHandler
    public void handle(ServerKickEvent event) {
        ProxiedPlayer p = event.getPlayer();
        ConnectState state = states.get(p.getUniqueId());
        if (state != null && state.getConnect() != null) {// maybe not peers
            event.setCancelled(true);
            event.setCancelServer(state.getConnect());
            if (Constants.DEBUG) {
                getLogger().info(String.format("KICK id=%s, to=%s", p.getUniqueId(), state.getConnect().getName()));
            }
        }
    }

    @EventHandler
    public void handle(ServerConnectedEvent event) {
        ConnectState state = states.remove(event.getPlayer().getUniqueId());
        if (state != null && state.getContents() != null) {
            Server sender = getSender(event.getServer());
            sender.sendData(Constants.PLUGIN_CHANNEL, state.getContents().encode());
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
        states.remove(event.getPlayer().getUniqueId());
    }
}
