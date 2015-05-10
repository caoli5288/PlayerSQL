package com.mengcraft.playersql.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.mengcraft.jdbc.ConnectionManager;
import com.mengcraft.playersql.DataCompond;

public class LoadTask implements Runnable {

    private static final String SELECT;
    private static final String INSERT;
    private static final String UPDATE;

    static {
        SELECT = "SELECT `Data`,`Online`,`Last` FROM `PlayerData` " +
                "WHERE `Player` = ?";
        INSERT = "INSERT INTO `PlayerData`(`Player`, `Online`) " +
                "VALUES(?, 1)";
        UPDATE = "UPDATE `PlayerData` SET `Online` = 1 " +
                "WHERE `Player` = ?";
    }

    private final ConnectionManager manager = ConnectionManager.DEFAULT;
    private final DataCompond compond = DataCompond.DEFAULT;
    private final UUID uuid;

    public LoadTask(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void run() {
        try {
            Connection c = manager.getConnection("playersql");
            PreparedStatement select = c.prepareStatement(SELECT);
            select.setString(1, uuid.toString());
            ResultSet result = select.executeQuery();
            if (!result.next()) {
                create(c);
                compond.map().put(uuid, DataCompond.STRING_EMPTY);
            } else if (result.getInt(2) == 0) {
                update(c);
                compond.map().put(uuid, result.getString(1));
            } else if (check(result.getLong(3)) > 5) {
                String data = result.getString(1);
                compond.map().put(uuid, data != null ?
                        data : DataCompond.STRING_EMPTY);
            } else {
                kick();
            }
            result.close();
            select.close();
            manager.release("playersql", c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void kick() {
        List<UUID> kick = compond.kick();
        synchronized (kick) {
            kick.add(uuid);
        }
    }

    private long check(long last) {
        return (System.currentTimeMillis() - last) / 60000;
    }

    private void update(Connection c) {
        try {
            PreparedStatement update = c.prepareStatement(UPDATE);
            update.setString(1, uuid.toString());
            update.executeUpdate();
            update.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void create(Connection c) {
        try {
            PreparedStatement insert = c.prepareStatement(INSERT);
            insert.setString(1, uuid.toString());
            insert.executeUpdate();
            insert.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
