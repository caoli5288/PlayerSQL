package com.mengcraft.playersql.locker;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.Lists;
import com.mengcraft.playersql.PluginMain;

import java.util.ArrayList;
import java.util.List;

import static com.mengcraft.playersql.UserManager.isLocked;

public class ProtocolBasedLocker extends PacketAdapter {

    public ProtocolBasedLocker(PluginMain plugin) {
        super(plugin, listListen());
    }

    static List<PacketType> listListen() {
        ArrayList<PacketType> all = Lists.newArrayList(PacketType.Play.Client.getInstance());
        all.remove(PacketType.Play.Client.TELEPORT_ACCEPT);
        all.remove(PacketType.Play.Client.KEEP_ALIVE);
        return all;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        if (isLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

}
