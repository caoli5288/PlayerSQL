package com.mengcraft.playersql.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.mengcraft.jdbc.ConnectionManager;
import com.mengcraft.playersql.DataCompond;
import com.mengcraft.playersql.SyncManager.State;

public class LoadTask implements Runnable {

    private static final String SELECT;
    private static final String INSERT;
    private static final String UPDATE;

    static {
        SELECT = "SELECT `Data`,`Online`,`Last` FROM `PlayerData` " +
                "WHERE `Player` = ?";
        INSERT = "INSERT INTO `PlayerData`(`Player`,`Online`,`LAST`) " +
                "VALUES(?,1,?)";
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
                PreparedStatement insert = c.prepareStatement(INSERT);
                insert.setString(1, uuid.toString());
                insert.setLong(2, System.currentTimeMillis());
                insert.executeUpdate();
                insert.close();
                
                compond.map().put(uuid, DataCompond.STRING_EMPTY);
                compond.state(uuid, State.JOIN_DONE);
            } else if (result.getInt(2) == 0) {
                PreparedStatement update = c.prepareStatement(UPDATE);
                update.setString(1, uuid.toString());
                update.executeUpdate();
                update.close();
                
                compond.map().put(uuid, result.getString(1));
                compond.state(uuid, State.JOIN_DONE);
            } else if (check(result.getLong(3)) > 5) {
                String data = result.getString(1);
                compond.map().put(uuid, data != null ?
                        data : DataCompond.STRING_EMPTY);
                compond.state(uuid, State.JOIN_DONE);
            } else {
                compond.state(uuid, State.JOIN_FAID);
            }
            result.close();
            select.close();
            manager.release("playersql", c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private long check(long last) {
        return (System.currentTimeMillis() - last) / 60000;
    }

}
