package com.mengcraft.playersql.task;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.mengcraft.playersql.Configs;
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
                sempty(uuid);
            } else {
                normal(uuid, data);
            }
        }
        synchronized (kick) {
            checkKick();
        }
    }

    private void sempty(UUID uuid) {
        compond.unlock(uuid);
        scheduleTask(uuid);
        if (Configs.DEBUG) {
            main.info("#5 New player " + uuid + ".");
        }
    }

    private void normal(UUID uuid, String data) {
        Player p = server.getPlayer(uuid);
        manager.load(p, data);
        scheduleTask(uuid);
        if (Configs.DEBUG) {
            main.info("#1 Load " + uuid + " data done.");
        }
    }

    private void checkKick() {
        for (UUID uuid : kick) {
            server.getPlayer(uuid).kickPlayer(DataCompond.MESSAGE_KICK);
            compond.unlock(uuid);
            if (Configs.DEBUG) {
                main.warn("#2 Kick " + uuid + " in data locked.");
            }
        }
        kick.clear();
    }

    private void scheduleTask(UUID uuid) {
        Map<UUID, Integer> task = compond.task();
        if (task.get(uuid) != null) {
            server.getScheduler().cancelTask(task.remove(uuid));
            if (Configs.DEBUG) {
                main.warn("#3 Cancel exists timer task for " + uuid + ".");
            }
        }
        Runnable runnable = new TimerSaveTask(server, uuid);
        int id = scheduleTask(runnable, 3600, 3600);
        compond.task().put(uuid, id);
        if (Configs.DEBUG) {
            main.info("#4 Schedule a timer task for " + uuid + ".");
        }
    }

    private int scheduleTask(Runnable runnable, int i, int j) {
        return scheduler.runTaskTimer(main, runnable, i, j).getTaskId();
    }

}
