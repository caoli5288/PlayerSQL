package com.mengcraft.playersql;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {

    public static final boolean SYN_CHEST;
    public static final boolean SYN_EFFECT;
    public static final boolean SYN_EXP;
    public static final boolean SYN_FOOD;
    public static final boolean SYN_HEALTH;
    public static final boolean SYN_INVENTORY;

    public static final int SYN_DELAY;

    public static final boolean MSG_ENABLE;
    public static final String MSG_LOADING;

    public static final String MSG_SYNCHRONIZED;
    public static final File FILE;
    public static final Configuration CONF;

    public static final boolean DEBUG;

    static {
        FILE = new File("plugins/PlayerSQL/config.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(FILE);
        SYN_HEALTH = yml.getBoolean("sync.health", true);
        SYN_CHEST = yml.getBoolean("sync.chest", true);
        SYN_INVENTORY = yml.getBoolean("sync.inventory", true);
        SYN_EFFECT = yml.getBoolean("sync.potion", true);
        SYN_EXP = yml.getBoolean("sync.exp", true);
        SYN_FOOD = yml.getBoolean("sync.food", true);
        SYN_DELAY = yml.getInt("sync.delay", 30);
        MSG_ENABLE = yml.getBoolean("plugin.messages", false);
        MSG_LOADING = ChatColor.translateAlternateColorCodes('&', yml.getString("messages.dataLoading", "&aPlease wait while your data is being loaded :)"));
        MSG_SYNCHRONIZED = ChatColor.translateAlternateColorCodes('&', yml.getString("messages.dataSynchronized", "&aEnjoy! All your data has been synchronized."));
        DEBUG = yml.getBoolean("plugin.debug", false);
        CONF = yml;
    }

}
