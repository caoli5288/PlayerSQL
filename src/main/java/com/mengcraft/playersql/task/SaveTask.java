package com.mengcraft.playersql.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import com.mengcraft.jdbc.ConnectionManager;

public class SaveTask implements Runnable {

    private static final String UPDATE;

    static {
        UPDATE = "UPDATE `PlayerData` " +
                "SET `Data` = ?, `Last` = ? , Online = ? " +
                "WHERE `Player` = ?";
    }

    private final String data;
    private final UUID uuid;
    private final ConnectionManager manager;
    private final int lock;

    public SaveTask(UUID uuid, String data, int lock) {
        this.uuid = uuid;
        this.data = data;
        this.lock = lock;
        manager = ConnectionManager.DEFAULT;
    }

    @Override
    public void run() {
        try {
            Connection c = manager.getConnection("playersql");
            PreparedStatement update = c.prepareStatement(UPDATE);
            update.setString(1, data);
            update.setLong(2, System.currentTimeMillis());
            update.setInt(3, lock);
            update.setString(4, uuid.toString());
            update.executeUpdate();
            update.close();
            manager.release("playersql", c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
