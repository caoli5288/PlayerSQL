package com.mengcraft.playersql.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import com.mengcraft.jdbc.ConnectionManager;
import com.mengcraft.playersql.DataCompond;

public class UnlockTask implements Runnable {

    private static final String COMMAND;

    static {
        COMMAND = "UPDATE `PlayerData` "
                + "SET `Online` = 0 "
                + "WHERE `Player` = ?";
    }

    private final UUID uuid;

    public UnlockTask(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void run() {
        try {
            Connection c = ConnectionManager.DEFAULT.getConnection("playersql");
            PreparedStatement unlock = c.prepareStatement(COMMAND);
            unlock.setString(1, uuid.toString());
            unlock.execute();
            unlock.close();
            ConnectionManager.DEFAULT.release("playersql", c);
            DataCompond.DEFAULT.map().put(uuid, DataCompond.STRING_SPECI);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
