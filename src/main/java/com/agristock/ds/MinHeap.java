package com.agristock.ds;

import java.util.Comparator;

/** Array-based binary min-heap. Root is always the smallest element per the comparator. */
public class MinHeap<T> {
    protected Object[] heap = new Object[16];
    protected int size;
    protected final Comparator<T> cmp;

    public MinHeap(Comparator<T> cmp) { this.cmp = cmp; }

    @SuppressWarnings("unchecked")
    private T at(int i) { return (T) heap[i]; }

    /** Insert with heapify-up: O(log n). */
    public void insert(T v) {
        if (size == heap.length) {
            Object[] bigger = new Object[heap.length * 2];
            for (int i = 0; i < size; i++) bigger[i] = heap[i];
            heap = bigger;
        }
        heap[size] = v;
        int cur = size++;
        while (cur > 0) {
            int parent = (cur - 1) / 2;
            if (cmp.compare(at(cur), at(parent)) < 0) { swap(cur, parent); cur = parent; }
            else break;
        }
    }

    /** O(1) access to the root. */
    public T peek() { return size == 0 ? null : at(0); }

    /** Extract-min with heapify-down: O(log n). */
    public T extractMin() {
        if (size == 0) return null;
        T root = at(0);
        heap[0] = heap[--size];
        heap[size] = null;
        int index = 0;
        while (2 * index + 1 < size) {
            int left = 2 * index + 1, right = left + 1, smallest = index;
            if (cmp.compare(at(left), at(smallest)) < 0) smallest = left;
            if (right < size && cmp.compare(at(right), at(smallest)) < 0) smallest = right;
            if (smallest == index) break;
            swap(index, smallest);
            index = smallest;
        }
        return root;
    }

    private void swap(int a, int b) { Object t = heap[a]; heap[a] = heap[b]; heap[b] = t; }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }
    public void clear() { heap = new Object[16]; size = 0; }

    /** Non-destructive sorted snapshot: copies the heap and repeatedly extracts the minimum. */
    public DynamicArray<T> sortedSnapshot() {
        MinHeap<T> copy = new MinHeap<>(cmp);
        for (int i = 0; i < size; i++) copy.insert(at(i));
        DynamicArray<T> out = new DynamicArray<>();
        while (!copy.isEmpty()) out.add(copy.extractMin());
        return out;
    }
}
