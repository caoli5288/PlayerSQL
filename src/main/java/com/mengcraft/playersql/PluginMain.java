package com.mengcraft.playersql;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.logging.Level;

/**
 * Created on 16-1-2.
 */
public class PluginMain extends JavaPlugin {

    public Player getPlayer(UUID uuid) {
        return getServer().getPlayer(uuid);
    }

    public void logException(Exception e) {
        getLogger().log(Level.WARNING, e.getMessage(), e);
    }
}
