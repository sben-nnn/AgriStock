package com.agristock.ds;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** FIFO queue built on linked nodes: inspection (M5), storage rotation (M6), pending sales (M9). */
public class LinkedQueue<T> implements Iterable<T> {
    private static class Node<T> {
        T data; Node<T> next;
        Node(T d) { data = d; }
    }

    private Node<T> front, rear;
    private int size;

    public void enqueue(T v) {
        Node<T> n = new Node<>(v);
        if (rear == null) front = rear = n;
        else { rear.next = n; rear = n; }
        size++;
    }

    public T dequeue() {
        if (front == null) throw new NoSuchElementException("Queue is empty");
        T d = front.data;
        front = front.next;
        if (front == null) rear = null;
        size--;
        return d;
    }

    public T peek() { return front == null ? null : front.data; }

    /** Remove a specific element (used when a pending sale is cancelled). */
    public boolean remove(T v) {
        Node<T> prev = null;
        for (Node<T> c = front; c != null; prev = c, c = c.next) {
            if (c.data == v) {
                if (prev == null) front = c.next; else prev.next = c.next;
                if (c == rear) rear = prev;
                size--;
                return true;
            }
        }
        return false;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            Node<T> cur = front;
            public boolean hasNext() { return cur != null; }
            public T next() { T d = cur.data; cur = cur.next; return d; }
        };
    }
}
