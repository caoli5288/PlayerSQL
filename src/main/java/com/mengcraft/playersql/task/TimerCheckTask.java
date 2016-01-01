package com.mengcraft.playersql.task;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.mengcraft.playersql.Config;
import com.mengcraft.playersql.DataCompound;
import com.mengcraft.playersql.Main;
import com.mengcraft.playersql.SyncManager;
import com.mengcraft.playersql.SyncManager.State;

public class TimerCheckTask implements Runnable {

    private final DataCompound compond;
    private final SyncManager manager;
    private final Main main;
    private final Server server;
    private final Map<UUID, String> map;
    private final BukkitScheduler scheduler;

    public TimerCheckTask(Main main) {
        this.compond = DataCompound.DEFAULT;
        this.manager = main.manager;
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
        if (p != null && p.isOnline()) {
            p.kickPlayer(DataCompound.MESSAGE_KICK);
        }
        compond.state(uuid, null);
    }

    private void load(UUID uuid) {
        String data = map.get(uuid);
        if (data == DataCompound.STRING_SPECI) {
            compond.state(uuid, null);
            compond.map().remove(uuid);
        } else if (data == DataCompound.STRING_EMPTY) {
            compond.state(uuid, null);
            compond.map().remove(uuid);
            if (Config.DEBUG) {
                main.info("#5 New player: " + uuid);
            }
            task(uuid);
            if (Config.MSG_ENABLE) main.getPlayer(uuid).sendMessage(
                    Config.MSG_SYNCHRONIZED);
        } else {
            manager.sync(uuid, data);
            if (Config.DEBUG) {
                main.info("#1 Synchronized data for " + uuid);
            }
            task(uuid);
            if (Config.MSG_ENABLE) main.getPlayer(uuid).sendMessage(
                    Config.MSG_SYNCHRONIZED);
        }
    }

    private void task(UUID uuid) {
        Map<UUID, Integer> task = compond.task();
        if (task.get(uuid) != null) {
            server.getScheduler().cancelTask(task.remove(uuid));
            if (Config.DEBUG) {
                main.warn("#3 Cancelled existing timer task for " + uuid);
            }
        }
        TimerSaveTask runnable = new TimerSaveTask(main, uuid);
        int id = scheduleTask(runnable, 3600, 3600);
        runnable.setId(id);
        compond.task().put(uuid, id);
        if (Config.DEBUG) {
            main.info("#4 Started a timer task for " + uuid);
        }
    }

    private int scheduleTask(Runnable runnable, int i, int j) {
        return scheduler.runTaskTimer(main, runnable, i, j).getTaskId();
    }

    public void register() {
        main.getServer().getScheduler().runTaskTimer(main, this, 0, 0);
    }

}
