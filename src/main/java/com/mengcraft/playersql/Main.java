package com.mengcraft.playersql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.mengcraft.playersql.SyncManager.State;
import com.mengcraft.playersql.jdbc.ConnectionFactory;
import com.mengcraft.playersql.jdbc.ConnectionHandler;
import com.mengcraft.playersql.jdbc.ConnectionManager;
import com.mengcraft.playersql.lib.ExpUtil;
import com.mengcraft.playersql.lib.ExpUtilHandler;
import com.mengcraft.playersql.lib.ItemUtil;
import com.mengcraft.playersql.lib.ItemUtilHandler;
import com.mengcraft.playersql.lib.Metrics;
import com.mengcraft.playersql.task.LoadTask;
import com.mengcraft.playersql.task.SwitchServerTask;
import com.mengcraft.playersql.task.TimerCheckTask;

public class Main extends JavaPlugin {

    public ItemUtil util;
    public ExpUtil exp;
    public SyncManager manager;

    private boolean enable;

    @Override
    public void onEnable() {
        try {
            util = new ItemUtilHandler(this).handle();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        exp = new ExpUtilHandler(this).handle();
        manager = new SyncManager(this);

        saveResource("config.yml", false);
        ConnectionFactory factory = new ConnectionFactory(
                getConfig().getString("plugin.database"),
                getConfig().getString("plugin.username"),
                getConfig().getString("plugin.password"));
        ConnectionManager manager = ConnectionManager.DEFAULT;
        ConnectionHandler handler = manager.getHandler("playersql", factory);

        try {
            Connection connection = handler.getConnection();
            String sql = "CREATE TABLE IF NOT EXISTS PlayerData("
                    + "`Id` int NOT NULL AUTO_INCREMENT, "
                    + "`Player` char(36) NULL, "
                    + "`Data` text NULL, "
                    + "`Online` int NULL, "
                    + "`Last` bigint NULL, "
                 // Instead of bigInt NULL, you could avoid manually setting the last time, and let SQL take care of this automatically:
                 // + "`Last` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
                    + "PRIMARY KEY(`Id`), "
                    + "INDEX `player_index` (`Player`));"; // Index on Player column will optimize lookups (fixes complain: https://www.spigotmc.org/resources/playersql.552/reviews#review-26795-20521 )
            Statement action = connection.createStatement();
            action.executeUpdate(sql);
            action.close();

            handler.release(connection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        new TimerCheckTask(this).register();
        new Executor(this).register();
        new SwitchServerTask(this).register();
        
        DataCompound compond = DataCompound.DEFAULT;
        for (Player p : getServer().getOnlinePlayers()) {
            UUID uuid = p.getUniqueId();
            compond.state(uuid, State.JOIN_WAIT);
            new LoadTask(uuid).run();
        }
        try {
            new Metrics(this).start();
        } catch (IOException e) {
            getLogger().warning(e.toString());
        }
        enable = true;
    }

    @Override
    public void onDisable() {
        if (enable) {
            DataCompound compond = DataCompound.DEFAULT;
            List<Player> list = new LinkedList<>();
            for (Player p : getServer().getOnlinePlayers()) {
                UUID uuid = p.getUniqueId();
                if (compond.state(uuid) == null) {
                    list.add(p);
                }
            }
            if (list.size() > 0) {
                manager.blockingSave(list, true);
            }
            ConnectionManager.DEFAULT.shutdown();
        }
    }

    public BukkitScheduler scheduler() {
        return getServer().getScheduler();
    }

    public void info(String string) {
        getLogger().info(string);
    }

    public void warn(String string) {
        getLogger().warning(string);
    }

    public Player getPlayer(UUID uuid) {
        return getServer().getPlayer(uuid);
    }

}
