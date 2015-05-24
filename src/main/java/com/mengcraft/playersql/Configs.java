package com.mengcraft.playersql;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Configs {

    public static final boolean SYN_HEAL;
    public static final boolean SYN_CEST;
    public static final boolean SYN_INVT;
    public static final boolean SYN_EFCT;
    public static final boolean SYN_EXPS;
    public static final boolean SYN_FOOD;
    
    public static final String MSG_LOADING;
    public static final String MSG_SYNCHRONIZED;
    
    public static final int SYN_DELY;
    public static final File FILE;
    public static final Configuration CONF;

    public static final boolean DEBUG;

    static {
        FILE = new File("plugins/PlayerSQL/config.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(FILE);
        SYN_HEAL = yml.getBoolean("sync.health", true);
        SYN_CEST = yml.getBoolean("sync.chest", true);
        SYN_INVT = yml.getBoolean("sync.inventory", true);
        SYN_EFCT = yml.getBoolean("sync.potion", true);
        SYN_EXPS = yml.getBoolean("sync.exp", true);
        SYN_FOOD = yml.getBoolean("sync.food", true);
        SYN_DELY = yml.getInt("sync.delay", 30);
        MSG_LOADING = ChatColor.translateAlternateColorCodes('&', yml.getString("messages.dataLoading", "&aPlease wait while your data is being loaded :)"));
        MSG_SYNCHRONIZED = ChatColor.translateAlternateColorCodes('&', yml.getString("messages.dataSynchronized", "&aEnjoy! All your data has been synchronized."));
        DEBUG = yml.getBoolean("plugin.debug", false);
        CONF = yml;
    }

}
