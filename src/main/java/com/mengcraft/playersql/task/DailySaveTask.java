package com.mengcraft.playersql.task;

import com.mengcraft.playersql.Config;
import com.mengcraft.playersql.User;
import com.mengcraft.playersql.UserManager;
import org.bukkit.Bukkit;

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
        User user = userManager.getUserData(this.uuid, false);
        if (user == null) {
            if (Config.DEBUG) {
                userManager.getMain().log("Cancel task for " + uuid + " offline!");
            }
            Bukkit.getScheduler().cancelTask(taskId);
        } else {
            this.saveCount++;
            if (Config.DEBUG) {
                userManager.getMain().log("Save user " + this.uuid + " count " + this.saveCount + '.');
            }
            userManager.getMain().runAsync(() -> userManager.saveUser(user, true));
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
