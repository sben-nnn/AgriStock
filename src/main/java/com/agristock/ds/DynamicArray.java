package com.agristock.ds;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DynamicArray<T> implements Iterable<T> {
    private Object[] data = new Object[16];
    private int size;

    public void add(T v) {
        if (size == data.length) {
            Object[] bigger = new Object[data.length * 2];
            for (int i = 0; i < size; i++) bigger[i] = data[i];
            data = bigger;
        }
        data[size++] = v;
    }

    @SuppressWarnings("unchecked")
    public T get(int i) {
        if (i < 0 || i >= size) throw new IndexOutOfBoundsException("Index " + i);
        return (T) data[i];
    }

    public void set(int i, T v) {
        if (i < 0 || i >= size) throw new IndexOutOfBoundsException("Index " + i);
        data[i] = v;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }
    public void clear() { data = new Object[16]; size = 0; }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            int i = 0;
            public boolean hasNext() { return i < size; }
            public T next() { if (i >= size) throw new NoSuchElementException(); return get(i++); }
        };
    }
}
