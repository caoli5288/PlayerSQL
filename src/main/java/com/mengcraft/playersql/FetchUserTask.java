package com.mengcraft.playersql;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created on 16-1-2.
 */
public class FetchUserTask implements Runnable {

    private EventExecutor executor;
    private UUID uuid;

    private int taskId;
    private int retryCount;
    private Player handle;

    @Override
    public synchronized void run() {
        User user = this.executor.getUserManager().fetchUser(this.uuid);
        if (user == null) {
            this.executor.getUserManager().cacheUser(this.uuid);
            this.executor.unlock(this.uuid);
            this.executor.cancelTask(this.taskId);
        } else if (user.isLocked() && this.retryCount++ < 5) {
            if (Config.DEBUG) {
                this.executor.getMain().logException(new PluginException("Fetch " + this.uuid + " retry " + this.retryCount + '.'));
            }
        } else {
            this.executor.getUserManager().cacheUser(this.uuid, user);
            this.executor.getUserManager().addFetched(user);
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
