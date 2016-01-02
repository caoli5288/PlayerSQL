package com.mengcraft.playersql;

import com.mengcraft.playersql.lib.ExpUtil;
import com.mengcraft.playersql.lib.ExpUtilHandler;
import com.mengcraft.playersql.lib.ItemUtil;
import com.mengcraft.playersql.lib.ItemUtilHandler;
import com.mengcraft.simpleorm.EbeanHandler;
import com.mengcraft.simpleorm.EbeanManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.logging.Level;

/**
 * Created on 16-1-2.
 */
public class PluginMain extends JavaPlugin {

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        ItemUtil itemUtil = new ItemUtilHandler(this).handle();
        ExpUtil expUtil = new ExpUtilHandler(this).handle();

        EbeanHandler db = EbeanManager.DEFAULT.getHandler(this);
        if (db.isNotInitialized()) {
            db.define(User.class);
            try {
                db.initialize();
            } catch (Exception e) {
                throw new PluginException("Can't connect to database!", e);
            }
        }
        db.install();
        db.reflect();

        UserManager userManager = new UserManager();
        userManager.setMain(this);
        userManager.setItemUtil(itemUtil);
        userManager.setExpUtil(expUtil);

        getServer().getScheduler().runTaskTimer(this, () -> {
            userManager.pendFetched();
        }, 1, 1);

        EventExecutor eventExecutor = new EventExecutor();
        eventExecutor.setMain(this);
        eventExecutor.setUserManager(userManager);

        getServer().getPluginManager().registerEvents(eventExecutor, this);
    }

    public Player getPlayer(UUID uuid) {
        return getServer().getPlayer(uuid);
    }

    public void logException(Exception e) {
        getLogger().log(Level.WARNING, e.getMessage(), e);
    }

    public BukkitTask runTaskTimerAsynchronously(Runnable r, int i) {
        return getServer().getScheduler().runTaskTimerAsynchronously(this, r, i, i);
    }

}
