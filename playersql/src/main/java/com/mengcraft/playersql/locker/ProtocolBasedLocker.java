package com.mengcraft.playersql.locker;

//import com.comphenix.protocol.PacketType;
//import com.comphenix.protocol.events.PacketAdapter;
//import com.comphenix.protocol.events.PacketEvent;
//import com.comphenix.protocol.events.PacketListener;
import com.google.common.collect.Lists;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

import static com.mengcraft.playersql.UserManager.isLocked;

public class ProtocolBasedLocker {

//    static class Listener extends PacketAdapter {
//
//        public Listener(JavaPlugin plugin) {
//            super(plugin, listen());
//        }
//
//        @Override
//        public void onPacketReceiving(PacketEvent event) {
//            if (isLocked(event.getPlayer().getUniqueId())) {
//                event.setCancelled(true);
//            }
//        }
//    }
//
//    private static List<PacketType> listen() {
//        ArrayList<PacketType> all = Lists.newArrayList(PacketType.Play.Client.getInstance());
//        all.remove(PacketType.Play.Client.TELEPORT_ACCEPT);
//        all.remove(PacketType.Play.Client.KEEP_ALIVE);
//        all.remove(PacketType.Play.Client.CUSTOM_PAYLOAD);
//        return all;
//    }
//
//    public static PacketListener b(JavaPlugin plugin) {
//        return new Listener(plugin);
//    }

}
