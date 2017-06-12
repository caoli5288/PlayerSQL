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
                this.executor.getMain().log("User data " + uuid + " not found!");
            }

            this.executor.getManager().unlockUser(this.uuid, true);
            this.executor.getManager().newUser(this.uuid);
            this.executor.getManager().createTask(this.uuid);

            if (Config.DEBUG) {
                this.executor.getMain().log("New user data for" + uuid + '.');
            }
        } else if (user.isLocked() && this.retry++ < 8) {
            if (Config.DEBUG) {
                this.executor.getMain().log("Load user data " + uuid + " fail " + retry + '.');
            }
        } else {
            this.executor.getManager().addFetched(user);
            if (Config.DEBUG) {
                this.executor.getMain().log("Load user data " + uuid + " done.");
            }

            this.executor.cancelTask(this.taskId);
            this.executor.getManager().lockUserData(uuid);
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
