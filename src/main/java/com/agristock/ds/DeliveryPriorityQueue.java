package com.agristock.ds;

import com.agristock.model.Models.Delivery;
import java.util.Comparator;

/** Priority queue for deliveries. Lower urgency number = higher priority. */
public class DeliveryPriorityQueue {
    private Delivery[] heap = new Delivery[16];
    private int size;

    public void add(Delivery d) {
        if (size == heap.length) {
            Delivery[] bigger = new Delivery[heap.length * 2];
            for (int i = 0; i < size; i++) bigger[i] = heap[i];
            heap = bigger;
        }
        heap[size] = d;
        int cur = size++;
        while (cur > 0) {
            int parent = (cur - 1) / 2;
            if (heap[cur].urgency < heap[parent].urgency) {
                swap(cur, parent);
                cur = parent;
            } else break;
        }
    }

    /** Remove and return the highest-priority delivery. */
    public Delivery poll() {
        if (size == 0) return null;
        Delivery top = heap[0];
        heap[0] = heap[--size];
        heap[size] = null;
        int index = 0;
        while (2 * index + 1 < size) {
            int left = 2 * index + 1, right = left + 1, smallest = index;
            if (heap[left].urgency < heap[smallest].urgency) smallest = left;
            if (right < size && heap[right].urgency < heap[smallest].urgency) smallest = right;
            if (smallest == index) break;
            swap(index, smallest);
            index = smallest;
        }
        return top;
    }

    /** See the highest-priority delivery without removing it. */
    public Delivery peek() { return size == 0 ? null : heap[0]; }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    private void swap(int a, int b) { Delivery t = heap[a]; heap[a] = heap[b]; heap[b] = t; }
}
