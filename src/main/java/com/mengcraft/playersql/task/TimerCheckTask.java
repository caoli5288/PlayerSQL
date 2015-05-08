package com.mengcraft.playersql.task;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.mengcraft.playersql.DataCompond;
import com.mengcraft.playersql.Main;
import com.mengcraft.playersql.SyncManager;

public class TimerCheckTask implements Runnable {

    private final DataCompond compond;
    private final SyncManager manager;
    private final Main main;
    private final Server server;
    private final List<UUID> kick;
    private final Map<UUID, String> map;
    private final BukkitScheduler scheduler;

    public TimerCheckTask(Main main) {
        this.compond = DataCompond.DEFAULT;
        this.manager = SyncManager.DEFAULT;
        this.main = main;
        this.server = main.getServer();
        this.scheduler = server.getScheduler();
        this.kick = compond.kick();
        this.map = compond.map();
    }

    @Override
    public void run() {
        List<UUID> list = compond.entry();
        for (UUID uuid : list) {
            String data = map.get(uuid);
            if (data == DataCompond.STRING_SPECI) {
                compond.unlock(uuid);
            } else if (data == DataCompond.STRING_EMPTY) {
                compond.unlock(uuid);
                scheduleTask(uuid);
            } else {
                Player p = server.getPlayer(uuid);
                manager.load(p, data);
                scheduleTask(uuid);
            }
        }
        synchronized (kick) {
            checkKick();
        }
    }

    private void checkKick() {
        for (UUID uuid : kick) {
            server.getPlayer(uuid).kickPlayer(DataCompond.MESSAGE_KICK);
            compond.unlock(uuid);
        }
        kick.clear();
    }

    private void scheduleTask(UUID uuid) {
        Map<UUID, Integer> task = compond.task();
        if (task.get(uuid) != null) {
            server.getScheduler().cancelTask(task.remove(uuid));
        }
        Runnable runnable = new TimerSaveTask(server, uuid);
        int id = scheduleTask(runnable, 3600, 3600);
        compond.task().put(uuid, id);
    }

    private int scheduleTask(Runnable runnable, int i, int j) {
        return scheduler.runTaskTimer(main, runnable, i, j).getTaskId();
    }

}
