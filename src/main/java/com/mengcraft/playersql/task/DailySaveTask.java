package com.mengcraft.playersql.task;

import com.mengcraft.playersql.Config;
import com.mengcraft.playersql.User;
import com.mengcraft.playersql.UserManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

import static com.mengcraft.playersql.PluginMain.nil;

/**
 * Created on 16-1-4.
 */
public class DailySaveTask extends BukkitRunnable {

    private UserManager manager = UserManager.INSTANCE;
    private UUID who;
    private int count;

    @Override
    public void run() {
        User user = manager.getUserData(this.who, false);
        if (nil(user)) {
            if (Config.DEBUG) {
                manager.getMain().log("Cancel task for " + who + " offline!");
            }
            cancel();
        } else {
            this.count++;
            if (Config.DEBUG) {
                manager.getMain().log("Save user " + this.who + " count " + this.count + '.');
            }
            manager.getMain().runAsync(() -> manager.saveUser(user, true));
        }
    }

    public void setWho(UUID who) {
        this.who = who;
    }

}
