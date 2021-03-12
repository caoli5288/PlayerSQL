package com.mengcraft.playersql;

import io.netty.channel.Channel;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Utils {

    private static final Function<Player, Channel> FUNCTION_getChannel;
    private static final BiFunction<Player, String, ?> FUNCTION_addChannel;

    static {
        ScriptEngine context = new ScriptEngineManager().getEngineByName("nashorn");
        FUNCTION_getChannel = getInterface(context, Function.class, "function apply(p) {\n" +
                "    return p.handle.playerConnection.networkManager.channel\n" +
                "}");
        FUNCTION_addChannel = getInterface(context, BiFunction.class, "function apply(p, ch) {\n" +
                "    p.addChannel(ch)\n" +
                "}");
    }

    @SneakyThrows
    private static <T> T getInterface(ScriptEngine context, Class<T> cls, String s) {
        Object obj = context.eval("(" + s +
                ")");
        return ((Invocable) context).getInterface(obj, cls);
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
        FUNCTION_addChannel.apply(p, s);
    }
}
