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
import com.mengcraft.playersql.SyncManager.State;

public class TimerCheckTask implements Runnable {

    private final DataCompond compond;
    private final SyncManager manager;
    private final Main main;
    private final Server server;
    private final Map<UUID, String> map;
    private final BukkitScheduler scheduler;

    public TimerCheckTask(Main main) {
        this.compond = DataCompond.DEFAULT;
        this.manager = SyncManager.DEFAULT;
        this.main = main;
        this.server = main.getServer();
        this.scheduler = server.getScheduler();
        this.map = compond.map();
    }

    @Override
    public void run() {
        List<UUID> list = compond.keys();
        for (UUID uuid : list) {
            State state = compond.state(uuid);
            if (state == State.JOIN_DONE) {
                load(uuid);
            } else if (state == State.JOIN_FAID) {
                kick(uuid);
            }
        }
    }

    private void kick(UUID uuid) {
        Player p = server.getPlayer(uuid);
        /*
         * Possible offline here.
         */
        if (p.isOnline()) {
            p.kickPlayer(DataCompond.MESSAGE_KICK);
        }
        compond.state(uuid, null);
    }

    private void load(UUID uuid) {
        String data = map.get(uuid);
        if (data == DataCompond.STRING_SPECI) {
            compond.state(uuid, null);
            compond.map().remove(uuid);
        } else if (data == DataCompond.STRING_EMPTY) {
            compond.state(uuid, null);
            compond.map().remove(uuid);
            if (Configs.DEBUG) {
                main.info("#5 New player " + uuid + ".");
            }
            task(uuid);
        } else {
            manager.load(server.getPlayer(uuid), uuid , data);
            if (Configs.DEBUG) {
                main.info("#1 Load " + uuid + " done..");
            }
            task(uuid);
        }
    }

    private void task(UUID uuid) {
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
