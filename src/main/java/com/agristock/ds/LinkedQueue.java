package com.agristock.ds;

// Simple linked-list queue. First in, first out.
public class LinkedQueue<T> {
    private Node<T> front;
    private Node<T> rear;
    private int size;

    // Add an item to the back of the queue.
    public void add(T v) {
        Node<T> n = new Node<>(v);
        if (rear == null) {
            front = n;
            rear = n;
        } else {
            rear.next = n;
            rear = n;
        }
        size++;
    }

    // Remove and return the item at the front.
    public T poll() {
        if (front == null) return null;
        T v = front.value;
        front = front.next;
        if (front == null) rear = null;
        size--;
        return v;
    }

    // Return the item at the front without removing it.
    public T peek() { return front == null ? null : front.value; }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    public void clear() { front = null; rear = null; size = 0; }

    private static class Node<T> {
        T value;
        Node<T> next;
        Node(T v) { value = v; }
    }
}
