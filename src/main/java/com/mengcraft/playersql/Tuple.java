package com.mengcraft.playersql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@Data
@RequiredArgsConstructor
public class Tuple<K, V> {

    private final K key;
    private V value;
}
