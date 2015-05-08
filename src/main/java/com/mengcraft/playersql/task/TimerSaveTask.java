package com.mengcraft.playersql.task;

import java.util.UUID;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.mengcraft.playersql.DataCompond;
import com.mengcraft.playersql.SyncManager;

public class TimerSaveTask implements Runnable {

    private final Server server;
    private final UUID uuid;

    public TimerSaveTask(Server server, UUID uuid) {
        this.server = server;
        this.uuid = uuid;
    }

    @Override
    public void run() {
        Player p = server.getPlayer(uuid);
        if (p != null && p.isOnline()) {
            SyncManager.DEFAULT.save(p, false);
        } else {
            int id = DataCompond.DEFAULT.task().remove(uuid);
            server.getScheduler().cancelTask(id);
        }
    }

}
