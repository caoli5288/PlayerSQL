package com.mengcraft.playersql.inject;

import io.netty.channel.Channel;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import org.bukkit.entity.Player;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class HijUtils {

    @Delegate
    private final IScripting scripting = newInjector();

    /**
     * Prevents automatic reading of a player's packets,
     * and replaces channels to prevent any other ops resetting automatic reading.
     *
     * @param player the player
     * @param autoRead the states of automatic reading
     */
    public void setAutoRead(Player player, boolean autoRead) throws Exception {
        Channel ch = scripting.getChannel(player);
        if (ch.isOpen()) {
            if (autoRead) {
                if (ch instanceof ChannelProxies.IWrappedChannel) {
                    ch = ((ChannelProxies.IWrappedChannel) ch).channel();
                    scripting.setChannel(player, ch);
                }

                ch.config().setAutoRead(true);
            } else {
                ch.config().setAutoRead(false);
                scripting.setChannel(player, ChannelProxies.create(ch, new HijConfig(ch.config())));
            }
        }

    }

    @SneakyThrows
    private static IScripting newInjector() {
        ScriptEngine js = new ScriptEngineManager().getEngineByExtension("js");
        js.eval("function getChannel(player) {\n" +
                "    return player.handle.playerConnection.networkManager.channel\n" +
                "}\n" +
                "function setChannel(player, ch) {\n" +
                "    player.handle.playerConnection.networkManager.channel = ch\n" +
                "}\n" +
                "function addCustomChannel(player, ch) {\n" +
                "    player.addChannel(ch)\n" +
                "}\n");
        return ((Invocable) js).getInterface(IScripting.class);
    }

    public interface IScripting {

        Channel getChannel(Player player) throws Exception;

        void setChannel(Player player, Channel ch) throws Exception;

        void addCustomChannel(Player player, String channel);
    }
}
