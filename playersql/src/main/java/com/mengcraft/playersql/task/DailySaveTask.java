package com.mengcraft.playersql.task;

import com.mengcraft.playersql.Config;
import com.mengcraft.playersql.PlayerData;
import com.mengcraft.playersql.PluginMain;
import com.mengcraft.playersql.UserManager;
import lombok.RequiredArgsConstructor;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

import static com.mengcraft.playersql.PluginMain.nil;

/**
 * Created on 16-1-4.
 */
@RequiredArgsConstructor
public class DailySaveTask extends BukkitRunnable {

    private UserManager manager = UserManager.INSTANCE;
    private final Player player;
    private int count;

    @Override
    public void run() {
        PlayerData user = manager.getUserData(player, false);
        if (nil(user)) {
            if (Config.DEBUG) {
                manager.getMain().log("Cancel task for " + player.getName() + " offline!");
            }
            cancel();
        } else {
            this.count++;
            if (Config.DEBUG) {
                manager.getMain().log("Save user " + player.getName() + " count " + this.count + '.');
            }
			PluginMain.runAsync(() -> manager.saveUser(user, true));
        }
    }

}
