package com.mengcraft.playersql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataCompond {

    public static final DataCompond DEFAULT = new DataCompond();
    public static final String STRING_EMPTY = new String();
    public static final String STRING_SPECI = new String();
    public static final String MESSAGE_KICK;

    static {
        MESSAGE_KICK = "Your data is locked, login later.";
    }

    private final List<UUID> list = new ArrayList<>();
    private final Map<UUID, String> map = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> task = new HashMap<>();
    private final List<UUID> kick = new ArrayList<>();

    public boolean islocked(UUID uuid) {
        return list.contains(uuid);
    }

    public void lock(UUID uuid) {
        list.add(uuid);
    }

    public void unlock(UUID uuid) {
        list.remove(uuid);
    }

    public Map<UUID, String> map() {
        return map;
    }

    public List<UUID> kick() {
        return kick;
    }

    public List<UUID> entry() {
        return new ArrayList<>(map.keySet());
    }

    public Map<UUID, Integer> task() {
        return task;
    }

}
