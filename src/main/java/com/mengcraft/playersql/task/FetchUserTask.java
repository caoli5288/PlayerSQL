package com.mengcraft.playersql.task;

import com.mengcraft.playersql.Config;
import com.mengcraft.playersql.LocalDataMgr;
import com.mengcraft.playersql.PlayerData;
import com.mengcraft.playersql.PluginMain;
import com.mengcraft.playersql.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

import static com.mengcraft.playersql.PluginMain.nil;

/**
 * Created on 16-1-2.
 */
public class FetchUserTask extends BukkitRunnable {

    private final UserManager manager = UserManager.INSTANCE;
    private final PluginMain main;
    private final Player player;
    private final UUID id;
    private int retry;

    public FetchUserTask(PluginMain main, Player player) {
        this.main = main;
        this.player = player;
        id = player.getUniqueId();
    }

    @Override
    public synchronized void run() {
        PlayerData user = manager.fetchUser(id);
        if (nil(user)) {
            cancel();

            LocalDataMgr.transfer(player, true);

            if (Config.DEBUG) {
                main.log("User data " + player.getName() + " not found!");
            }

            manager.newUser(id);
            main.run(() -> {
                manager.unlockUser(id);
                manager.createTask(id);
            });

            if (Config.DEBUG) {
                main.log("New user data for" + player.getName() + '.');
            }
        } else if (user.isLocked() && this.retry++ < 8) {
            if (Config.DEBUG) {
                main.log("Load user " + player.getName() + " fail " + retry + '.');
            }
        } else {
            cancel();

            LocalDataMgr.transfer(player);// TODO move to server thread if any exception

            manager.addFetched(user);

            if (Config.DEBUG) {
                main.log("Load user " + player.getName() + " done.");
            }

            manager.updateDataLock(id, true);
        }
    }

}
