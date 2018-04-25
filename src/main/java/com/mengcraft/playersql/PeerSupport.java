package com.mengcraft.playersql;

import com.mengcraft.playersql.peer.DataBuf;
import com.mengcraft.playersql.peer.DataRequest;
import com.mengcraft.playersql.peer.IPacket;
import com.mengcraft.playersql.peer.PeerReady;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.*;

public class PeerSupport extends Plugin implements Listener {

    private final BiFunctionRegistry<ProxiedPlayer, IPacket, Void> registry = new BiFunctionRegistry<>();

    private final Set<UUID> handled = new HashSet<>();
    private final Map<UUID, Tuple<ServerInfo, DataBuf>> pending = new HashMap<>();

    {
        registry.register(IPacket.Protocol.PEER_READY, (p, ipk) -> {
            PeerReady pk = (PeerReady) ipk;
            handled.add(pk.getId());
            Tuple<ServerInfo, DataBuf> tuple = pending.remove(pk.getId());
            if (tuple == null || tuple.getValue() == null) {
                return null;
            }
            p.getServer().sendData(IPacket.Protocol.TAG, tuple.getValue().encode());
            return null;
        });
        registry.register(IPacket.Protocol.DATA_BUF, (p, ipk) -> {
            DataBuf pk = (DataBuf) ipk;
            Tuple<ServerInfo, DataBuf> tuple = pending.get(pk.getId());
            ServerInfo serv = tuple.getKey();
            handled.remove(pk.getId());
            p.connect(serv, (succ, err) -> {
                if (!succ || pk.getBuf().length == 0) {// recv zero if request when client not ready, we not redirect it
                    return;
                }
                tuple.setValue(pk);
            });
            return null;
        });
    }

    @Override
    public void onEnable() {
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
        if (!handled.contains(event.getPlayer().getUniqueId())) {
            return;
        }

        event.setCancelled(true);

        pending.put(event.getPlayer().getUniqueId(), new Tuple<>(event.getTarget()));

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
    public void handle(PlayerDisconnectEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        handled.remove(id);
        pending.remove(id);
    }
}
