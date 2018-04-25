package com.mengcraft.playersql;

import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {

    public static final Configuration CONF;

    public static final boolean SYN_CHEST;
    public static final boolean SYN_EFFECT;
    public static final boolean SYN_EXP;
    public static final boolean SYN_FOOD;
    public static final boolean SYN_HEALTH;
    public static final boolean SYN_INVENTORY;
    public static final boolean KICK_LOAD_FAILED;
    public static final boolean TRANSFER_ORIGIN;
    public static final boolean OMIT_PLAYER_DEATH;

    public static final String KICK_LOAD_MESSAGE;

    public static final boolean DEBUG;
    public static final int SYN_DELAY;

    static {
        CONF = JavaPlugin.getPlugin(PluginMain.class).getConfig();
        SYN_HEALTH = CONF.getBoolean("sync.health", true);
        SYN_CHEST = CONF.getBoolean("sync.chest", true);
        SYN_INVENTORY = CONF.getBoolean("sync.inventory", true);
        SYN_EFFECT = CONF.getBoolean("sync.potion", true);
        SYN_EXP = CONF.getBoolean("sync.exp", true);
        SYN_FOOD = CONF.getBoolean("sync.food", true);
        SYN_DELAY = CONF.getInt("plugin.delay", 30);
        DEBUG = CONF.getBoolean("plugin.debug", false);
        OMIT_PLAYER_DEATH = CONF.getBoolean("plugin.omit-player-death", false);
        KICK_LOAD_FAILED = CONF.getBoolean("kick-load-failed", true);
        KICK_LOAD_MESSAGE = CONF.getString("kick-load-message", "Your game data loading error, please contact the operator");
        TRANSFER_ORIGIN = CONF.getBoolean("transfer-origin", false);
    }

}
