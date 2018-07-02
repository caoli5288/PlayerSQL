package com.mengcraft.playersql;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class BiRegistry<T, U> {

    private final Map<Object, BiConsumer<T, U>> all;

    public BiRegistry(Map<Object, BiConsumer<T, U>> all) {
        this.all = all;
    }

    public BiRegistry() {
        all = new HashMap<>();
    }

    public void register(Object key, BiConsumer<T, U> function) {
        all.put(key, function);
    }

    public void handle(Object key, T i, U l) {
        if (all.containsKey(key)) {
            all.get(key).accept(i, l);
        }
    }

    public Set<Object> getKeys() {
        return all.keySet();
    }

}
