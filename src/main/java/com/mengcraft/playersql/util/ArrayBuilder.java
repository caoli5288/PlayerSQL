package com.mengcraft.playersql.util;

import java.lang.reflect.Array;

public class ArrayBuilder<T> {

    private static final int SIZE_DEFAULT = 8;
    private Object[] array;
    private int cursor;

    public ArrayBuilder() {
        array = new Object[] {};
    }

    public void append(T value) {
        if (cursor >= array.length) {
            growArray();
        }
        array[cursor++] = value;
    }

    @SuppressWarnings("unchecked")
    public T[] build(Class<T> type) {
        Object[] output = (Object[]) Array.newInstance(type, cursor);
        while (cursor != 0) {
            output[--cursor] = array[cursor];
        }
        return (T[]) output;
    }

    private void growArray() {
        final Object[] bigger;
        if (array.length < 1) {
            bigger = new Object[SIZE_DEFAULT];
        } else {
            bigger = new Object[array.length * 2];
        }
        for (int i = 0; i != array.length;) {
            bigger[i] = array[i++];
        }
        this.array = bigger;
    }

}
