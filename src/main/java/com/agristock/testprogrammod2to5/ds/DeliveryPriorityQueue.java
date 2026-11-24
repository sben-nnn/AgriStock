package com.agristock.testprogrammod2to5.ds;

import java.util.Comparator;

// Priority queue (heap-backed). Highest priority = smallest key per comparator (Earliest Deadline First).
public class DeliveryPriorityQueue<T> extends MinHeap<T> {
    public DeliveryPriorityQueue(Comparator<T> cmp) { super(cmp); }
    public void enqueue(T v) { insert(v); }
    public T dequeue() { return extractMin(); }
}
