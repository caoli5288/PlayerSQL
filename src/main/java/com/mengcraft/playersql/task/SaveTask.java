package com.mengcraft.playersql.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;
import com.mengcraft.jdbc.ConnectionManager;

public class SaveTask implements Runnable {

    private static final String UPDATE;

    static {
        UPDATE = "UPDATE `PlayerData` " +
                "SET `Data` = ?, `Last` = ? , Online = ? " +
                "WHERE `Player` = ?";
    }

    private final Map<UUID, String> map;
    private final int lock;

    public SaveTask(UUID uuid, String data, boolean unlock) {
        if (uuid == null || data == null) {
            throw new NullPointerException();
        }
        this.map = ImmutableMap.of(uuid, data);
        this.lock = unlock ? 0 : 1;
    }

    public SaveTask(Map<UUID, String> map, boolean unlock) {
        if (map.size() == 0) {
            throw new IllegalArgumentException("Empty map!");
        }
        this.map = map;
        this.lock = unlock ? 0 : 1;
    }

    @Override
    public void run() {
        ConnectionManager manager = ConnectionManager.DEFAULT;
        try {
            Connection c = manager.getConnection("playersql");
            PreparedStatement update = c.prepareStatement(UPDATE);
            for (Entry<UUID, String> entry : map.entrySet()) {
                update.setString(1, entry.getValue());
                update.setLong(2, System.currentTimeMillis());
                update.setInt(3, lock);
                update.setString(4, entry.getKey().toString());
                update.addBatch();
            }
            update.executeBatch();
            update.close();
            manager.release("playersql", c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
