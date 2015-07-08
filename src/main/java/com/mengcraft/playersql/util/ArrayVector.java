package com.mengcraft.playersql.util;

import java.util.Iterator;

public class ArrayVector<E> implements Iterator<E> {

    private final E[] array;
    private int cursor;

    @Override
    public boolean hasNext() {
        return array.length != cursor;
    }

    @Override
    public E next() {
        return hasNext() ? array[cursor++] : null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    public boolean contains(E element) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(element)) { return true; }
        }
        return false;
    }

    public E get(int index) {
        if (index < 0 || index >= array.length) {
            throw new IndexOutOfBoundsException();
        }
        return array[index];
    }

    public E get() {
        return hasNext() ? array[cursor] : null;
    }

    public int remain() {
        return array.length - cursor;
    }

    public int cursor() {
        return cursor;
    }

    public E[] array() {
        return array;
    }

    @SuppressWarnings("unchecked")
    public ArrayVector(E... array) {
        if (array == null) {
            throw new NullPointerException();
        }
        this.array = array;
    }

}
