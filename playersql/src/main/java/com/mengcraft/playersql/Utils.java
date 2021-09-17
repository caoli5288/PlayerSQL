package com.mengcraft.playersql;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;
import com.comphenix.protocol.utility.MinecraftReflection;
import io.netty.channel.Channel;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Utils {

    private static final Method METHOD_addChannel = getMethod(MinecraftReflection.getCraftPlayerClass(), "addChannel", String.class);
    private static final PlayerInjectionHandler PLAYER_INJECTION_HANDLER = init_PLAYER_INJECTION_HANDLER();

    private static PlayerInjectionHandler init_PLAYER_INJECTION_HANDLER() {
        PacketFilterManager pfm = (PacketFilterManager) ProtocolLibrary.getProtocolManager();
        try {
            Field f = PacketFilterManager.class.getDeclaredField("playerInjection");
            f.setAccessible(true);
            return (PlayerInjectionHandler) f.get(pfm);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Method getMethod(Class<?> cls, String methodName, Class<?>... paramTypes) {
        try {
            Method obj = cls.getDeclaredMethod(methodName, paramTypes);
            obj.setAccessible(true);
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Channel getChannel(Player player) {
        return PLAYER_INJECTION_HANDLER.getChannel(player);
    }

    public static void setAutoRead(Player p, boolean b) {
        Channel channel = getChannel(p);
        if (channel.isOpen()) {
            channel.eventLoop().execute(() -> channel.config().setAutoRead(b));
        }
    }

    public static void addChannel(Player p, String s) {
        try {
            METHOD_addChannel.invoke(p, s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
