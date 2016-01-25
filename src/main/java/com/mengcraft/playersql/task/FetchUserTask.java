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
    private int retryCount;

    @Override
    public synchronized void run() {
        User user = this.executor.getUserManager().fetchUser(this.uuid);
        if (user == null) {
            this.executor.getUserManager().cacheUser(this.uuid);
            this.executor.getUserManager().saveUser(this.uuid, true);
            this.executor.getUserManager().createTask(this.uuid);
            this.executor.getUserManager().unlockUser(this.uuid, true);
            if (Config.DEBUG) {
                this.executor.getMain().logMessage("User data " + this.uuid + " not found!");
            }
            this.executor.cancelTask(this.taskId);
        } else if (user.isLocked() && this.retryCount++ < 8) {
            if (Config.DEBUG) {
                this.executor.getMain().logMessage("Load user data " + uuid + " fail " + retryCount + '.');
            }
        } else {
            this.executor.getUserManager().cacheUser(this.uuid, user);
            this.executor.getUserManager().addFetched(user);
            this.executor.getUserManager().saveUser(user, true);
            if (Config.DEBUG) {
                this.executor.getMain().logMessage("Load user data " + uuid + " done.");
                this.executor.getMain().logMessage("Lock user data " + uuid + " done.");
            }
            this.executor.cancelTask(this.taskId);
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
