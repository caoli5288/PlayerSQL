package com.mengcraft.playersql.lib;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

public class ExpUtilHandler {

    private final Server server;

    private ExpUtil handle;
    private String version;

    public ExpUtil handle() {
        if (handle == null) {
            if (test(version())) {
                handle = new ExpUtil.NewerExpUtil();
            } else {
                handle = new ExpUtil.OlderExpUtil();
            }
        }
        return handle;
    }

    private boolean test(String version) {
        return Integer.parseInt(version.split("_")[1]) > 7;
    }

    private String version() {
        if (version == null) {
            version = server.getClass().getName().split("\\.")[3];
        }
        return version;
    }

    public ExpUtilHandler(Plugin in) {
        if (in == null) {
            throw new NullPointerException();
        }
        this.server = in.getServer();
    }

}
