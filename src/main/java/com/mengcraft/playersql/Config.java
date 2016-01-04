package com.mengcraft.playersql;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {

    public static final Configuration CONF;

    public static final boolean SYN_CHEST;
    public static final boolean SYN_EFFECT;
    public static final boolean SYN_EXP;
    public static final boolean SYN_FOOD;
    public static final boolean SYN_HEALTH;
    public static final boolean SYN_INVENTORY;

    public static final boolean DEBUG;
    public static final int SYN_DELAY;

    static {
        CONF = YamlConfiguration.loadConfiguration(new File("plugins/PlayerSQL/config.yml"));
        SYN_HEALTH = CONF.getBoolean("sync.health", true);
        SYN_CHEST = CONF.getBoolean("sync.chest", true);
        SYN_INVENTORY = CONF.getBoolean("sync.inventory", true);
        SYN_EFFECT = CONF.getBoolean("sync.potion", true);
        SYN_EXP = CONF.getBoolean("sync.exp", true);
        SYN_FOOD = CONF.getBoolean("sync.food", true);
        SYN_DELAY = CONF.getInt("plugin.delay", 30);
        DEBUG = CONF.getBoolean("plugin.debug", false);
    }

}
