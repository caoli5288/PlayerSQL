package com.mengcraft.playersql.task;

import com.mengcraft.playersql.Config;
import com.mengcraft.playersql.EventExecutor;
import com.mengcraft.playersql.PluginException;
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
            // Not lock in database. Not needed for fresh man.
            if (Config.DEBUG) {
                this.executor.getMain().logMessage("Fresh user " + this.uuid + '.');
            }
            this.executor.getUserManager().cacheUser(this.uuid);
            this.executor.cancelTask(this.taskId);
            this.executor.createTask(this.uuid);
            this.executor.getUserManager().unlockUser(this.uuid, true);
        } else if (user.isLocked() && this.retryCount++ < 8) {
            if (Config.DEBUG) {
                this.executor.getMain().logException(new PluginException("Fetch " + this.uuid + " retry " + this.retryCount + '.'));
            }
        } else {
            if (Config.DEBUG) {
                this.executor.getMain().logMessage("Load user " + this.uuid + " done. Scheduling store data.");
            }
            this.executor.getUserManager().cacheUser(this.uuid, user);
            this.executor.getUserManager().addFetched(user);
            this.executor.cancelTask(this.taskId);
            this.executor.createTask(this.uuid);
            this.executor.getUserManager().saveUser(user, true);
            if (Config.DEBUG) {
                this.executor.getMain().logMessage("Lock user " + this.uuid + " on database done.");
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
