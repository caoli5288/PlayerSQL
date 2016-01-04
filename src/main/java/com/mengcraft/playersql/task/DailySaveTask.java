package com.mengcraft.playersql.task;

import com.mengcraft.playersql.Config;
import com.mengcraft.playersql.EventExecutor;
import com.mengcraft.playersql.PluginException;
import com.mengcraft.playersql.User;

import java.util.UUID;

/**
 * Created on 16-1-4.
 */
public class DailySaveTask implements Runnable {

    private EventExecutor executor;
    private UUID uuid;

    private int taskId;
    private int saveCount;

    @Override
    public synchronized void run() {
        User user = this.executor.getUserManager().getUser(this.uuid);
        if (user == null) {
            if (Config.DEBUG) {
                this.executor.getMain().logException(new PluginException("User " + this.uuid + " not cached!"));
            }
            this.executor.cancelTask(this.taskId);
        } else {
            this.saveCount++;
            if (Config.DEBUG) {
                this.executor.getMain().logMessage("Save user " + this.uuid + " count " + this.saveCount + '.');
            }
            this.executor.getUserManager().syncUser(user);
            this.executor.getMain().runTaskAsynchronously(() -> this.executor.getUserManager().saveUser(user, true));
        }
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public void setExecutor(EventExecutor executor) {
        this.executor = executor;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

}
