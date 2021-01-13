package com.mengcraft.playersql;

import io.netty.channel.Channel;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Utils {

    private static final ScriptEngine ENGINE = new ScriptEngineManager(Utils.class.getClassLoader()).getEngineByExtension("js");

    private static final Function<Player, Channel> FUNCTION_getChannel = getInterface(Function.class, "function apply(p) {\n" +
            "    return p.handle.playerConnection.networkManager.channel\n" +
            "}");

    private static final BiConsumer<Player, String> FUNCTION_addChannel = getInterface(BiConsumer.class, "function accept(p, ch) {\n" +
            "    p.addChannel(ch)\n" +
            "}");

    @SneakyThrows
    private static <T> T getInterface(Class<T> cls, String s) {
        Object obj = ENGINE.eval("(" + s +
                ")");
        return ((Invocable) ENGINE).getInterface(obj, cls);
    }

    public static Channel getChannel(Player t) {
        return FUNCTION_getChannel.apply(t);
    }

    public static void setAutoRead(Player p, boolean b) {
        Channel channel = getChannel(p);
        if (channel.isOpen()) {
            channel.eventLoop().execute(() -> channel.config().setAutoRead(b));
        }
    }

    public static void addChannel(Player p, String s) {
        FUNCTION_addChannel.accept(p, s);
    }
}
