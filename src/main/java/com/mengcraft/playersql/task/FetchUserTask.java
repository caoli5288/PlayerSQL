package com.mengcraft.playersql.task;

import com.mengcraft.playersql.Config;
import com.mengcraft.playersql.PluginMain;
import com.mengcraft.playersql.User;
import com.mengcraft.playersql.UserManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

import static com.mengcraft.playersql.PluginMain.nil;

/**
 * Created on 16-1-2.
 */
public class FetchUserTask extends BukkitRunnable {

    private final UserManager manager = UserManager.INSTANCE;
    private final PluginMain main;
    private final UUID who;
    private int retry;

    public FetchUserTask(PluginMain main, UUID who) {
        this.main = main;
        this.who = who;
    }

    @Override
    public synchronized void run() {
        User user = manager.fetchUser(who);
        if (nil(user)) {
            cancel();

            if (Config.DEBUG) {
                main.log("User data " + who + " not found!");
            }

            manager.newUser(who);
            main.run(() -> {
                manager.unlockUser(who);
                manager.createTask(who);
            });

            if (Config.DEBUG) {
                main.log("New user data for" + who + '.');
            }
        } else if (user.isLocked() && this.retry++ < 8) {
            if (Config.DEBUG) {
                main.log("Load user data " + who + " fail " + retry + '.');
            }
        } else {
            cancel();

            manager.addFetched(user);
            if (Config.DEBUG) {
                main.log("Load user data " + who + " done.");
            }

            manager.updateDataLock(who, true);
        }
    }

}
