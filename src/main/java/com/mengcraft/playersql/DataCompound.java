package com.mengcraft.playersql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.mengcraft.playersql.SyncManager.State;

public class DataCompound {

    public static final DataCompound DEFAULT = new DataCompound();
    public static final String STRING_EMPTY = new String();
    public static final String STRING_SPECI = new String();
    public static final String MESSAGE_KICK;

    static {
        MESSAGE_KICK = "Your data is locked; login later.";
    }

    private final Map<UUID, State>  state = new ConcurrentHashMap<>();
    private final Map<UUID, String>  data = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> task = new HashMap<>();

    public Map<UUID, String> map() {
        return data;
    }

    public void state(UUID uuid, State s) {
        if (s != null) {
            state.put(uuid, s);
        } else {
            state.remove(uuid);
        }
    }

    public State state(UUID uuid) {
        return state.get(uuid);
    }

    /**
     * Get copy of state map's keys.
     * 
     * @return a list.
     */
    public List<UUID> keys() {
        return new ArrayList<>(state.keySet());
    }

    public Map<UUID, Integer> task() {
        return task;
    }

}
