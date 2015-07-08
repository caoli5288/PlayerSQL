package com.mengcraft.playersql;

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
import com.mengcraft.playersql.lib.ItemUtil;
import com.mengcraft.playersql.lib.ItemUtilHandler;
import com.mengcraft.playersql.task.LoadTask;
import com.mengcraft.playersql.task.TimerCheckTask;

public class Main extends JavaPlugin {

    public ItemUtil util;
    public SyncManager manager;
    
    private boolean enable;

    @Override
    public void onEnable() {
        try {
            util = new ItemUtilHandler(this).handle();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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
                    + "`Player` text NULL, "
                    + "`Data` text NULL, "
                    + "`Online` int NULL, "
                    + "`Last` bigint NULL, "
                    + "PRIMARY KEY(`Id`));";
            Statement action = connection.createStatement();
            action.executeUpdate(sql);
            action.close();
            handler.release(connection);
            scheduler().runTask(this, new MetricsTask(this));
            scheduler().runTaskTimer(this, new TimerCheckTask(this), 0, 0);
            register(new Events(this), this);
        } catch (Exception e) {
            getLogger().warning("Unable to connect to database.");
            setEnabled(false);
        }

        DataCompound compond = DataCompound.DEFAULT;
        for (Player p : getServer().getOnlinePlayers()) {
            UUID uuid = p.getUniqueId();
            compond.state(uuid, State.JOIN_WAIT);
            new LoadTask(uuid).run();
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

    private void register(Events events, Main main) {
        getServer().getPluginManager().registerEvents(events, main);
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
