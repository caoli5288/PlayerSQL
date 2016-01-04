package com.mengcraft.playersql;

import com.mengcraft.playersql.lib.*;
import com.mengcraft.simpleorm.EbeanHandler;
import com.mengcraft.simpleorm.EbeanManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
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

        UserManager userManager = UserManager.INSTANCE;
        userManager.setMain(this);
        userManager.setItemUtil(itemUtil);
        userManager.setExpUtil(expUtil);

        EventExecutor eventExecutor = new EventExecutor();
        eventExecutor.setMain(this);
        eventExecutor.setUserManager(userManager);

        getServer().getScheduler().runTaskTimer(this, () -> {
            userManager.pendFetched();
        }, 1, 1);

        getServer().getPluginManager().registerEvents(eventExecutor, this);

        try {
            new Metrics(this).start();
        } catch (IOException e) {
            logException(e);
        }
    }

    @Override
    public void onDisable() {
        for (Player p : getServer().getOnlinePlayers()) {
            UserManager.INSTANCE.saveUser(p.getUniqueId(), false);
        }
    }

    public Player getPlayer(UUID uuid) {
        return getServer().getPlayer(uuid);
    }

    public void logException(Exception e) {
        getLogger().log(Level.WARNING, e.getMessage(), e);
    }

    public void logMessage(String s) {
        getLogger().log(Level.INFO, s);
    }

    public BukkitTask runTaskTimerAsynchronously(Runnable r, int i) {
        return getServer().getScheduler().runTaskTimerAsynchronously(this, r, i, i);
    }

    public BukkitTask runTaskAsynchronously(Runnable r) {
        return getServer().getScheduler().runTaskAsynchronously(this, r);
    }

    public BukkitTask runTask(Runnable r) {
        return getServer().getScheduler().runTask(this, r);
    }

    public BukkitTask runTaskTimer(Runnable r, int i) {
        return getServer().getScheduler().runTaskTimer(this, r, i, i);
    }

}
