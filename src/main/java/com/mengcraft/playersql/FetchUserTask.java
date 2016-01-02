package com.mengcraft.playersql;

import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

/**
 * Created on 16-1-2.
 */
public class FetchUserTask implements Runnable {

    private EventExecutor executor;
    private BukkitTask task;
    private UUID uuid;

    private int retryCount;

    @Override
    public synchronized void run() {
        // TODO
    }

    public FetchUserTask setExecutor(EventExecutor executor) {
        synchronized (this) {
            this.executor = executor;
        }
        return this;
    }

    public FetchUserTask setUuid(UUID uuid) {
        synchronized (this) {
            this.uuid = uuid;
        }
        return this;
    }

    public FetchUserTask setTask(BukkitTask task) {
        synchronized (this) {
            this.task = task;
        }
        return this;
    }

}
