package com.mengcraft.playersql.task;

import com.mengcraft.playersql.Config;
import com.mengcraft.playersql.EventExecutor;
import com.mengcraft.playersql.User;

import java.util.UUID;

/**
 * Created on 16-1-2.
 */
public class FetchUserTask implements Runnable {

    private EventExecutor executor;
    private UUID uuid;

    private int taskId;
    private int retry;

    @Override
    public synchronized void run() {
        User user = this.executor.getManager().fetchUser(this.uuid);
        if (user == null) {
            this.executor.cancelTask(this.taskId);
            if (Config.DEBUG) {
                this.executor.getMain().info("User data " + uuid + " not found!");
            }

            this.executor.getManager().create(this.uuid);
            this.executor.getManager().createTask(this.uuid);
            this.executor.getManager().unlockUser(this.uuid, true);
        } else if (user.isLocked() && this.retry++ < 8) {
            if (Config.DEBUG) {
                this.executor.getMain().info("Load user data " + uuid + " fail " + retry + '.');
            }
        } else {
            this.executor.getManager().cache(this.uuid, user);
            this.executor.getManager().addFetched(user);
            if (Config.DEBUG) {
                this.executor.getMain().info("Load user data " + uuid + " done.");
            }

            this.executor.cancelTask(this.taskId);
            this.executor.getManager().saveUser(user, true);

            if (Config.DEBUG) {
                this.executor.getMain().info("Lock user data " + uuid + " done.");
            }
        }
    }

    public void setExecutor(EventExecutor executor) {
        this.executor = executor;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

}
