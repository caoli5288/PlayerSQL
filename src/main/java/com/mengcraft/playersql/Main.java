package com.mengcraft.playersql;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.mengcraft.jdbc.ConnectionFactory;
import com.mengcraft.jdbc.ConnectionHandler;
import com.mengcraft.jdbc.ConnectionManager;
import com.mengcraft.playersql.task.SaveTask;
import com.mengcraft.playersql.task.TimerCheckTask;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
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
    }

    @Override
    public void onDisable() {
        SyncManager manager = SyncManager.DEFAULT;
        DataCompond compond = DataCompond.DEFAULT;
        Map<UUID, String> map = new HashMap<>();
        for (Player p : getServer().getOnlinePlayers()) {
            UUID uuid = p.getUniqueId();
            if (!compond.islocked(uuid))
                map.put(uuid, manager.data(p));
        }
        if (map.size() != 0) {
            new SaveTask(map, true).run();
        }
        ConnectionManager.DEFAULT.shutdown();
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

}
