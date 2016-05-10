package com.mengcraft.playersql.task;

import com.mengcraft.playersql.Config;
import com.mengcraft.playersql.PluginException;
import com.mengcraft.playersql.User;
import com.mengcraft.playersql.UserManager;

import java.util.UUID;

/**
 * Created on 16-1-4.
 */
public class DailySaveTask implements Runnable {

    private UserManager userManager;
    private UUID uuid;

    private int taskId;
    private int saveCount;

    @Override
    public synchronized void run() {
        User user = userManager.getUser(this.uuid);
        if (user == null) {
            if (Config.DEBUG) {
                userManager.getMain().info(new PluginException("User " + this.uuid + " not cached!"));
            }
            userManager.cancelTask(this.taskId);
        } else {
            this.saveCount++;
            if (Config.DEBUG) {
                userManager.getMain().info("Save user " + this.uuid + " count " + this.saveCount + '.');
            }
            userManager.syncUser(user);
            userManager.getMain().runTaskAsynchronously(() -> userManager.saveUser(user, true));
        }
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

}
