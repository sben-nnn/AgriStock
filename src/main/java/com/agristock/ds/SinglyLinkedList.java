package com.agristock.ds;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/** Singly linked list: farmers (M2), stock records (M4), spoilage log (M11), audit trail (M14). */
public class SinglyLinkedList<T> implements Iterable<T> {
    private static class Node<T> {
        T data; Node<T> next;
        Node(T d) { data = d; }
    }

    private Node<T> head, tail;
    private int size;

    /** Insert at the head: O(1), no shifting of existing elements. */
    public void addFirst(T v) {
        Node<T> n = new Node<>(v);
        n.next = head;
        head = n;
        if (tail == null) tail = n;
        size++;
    }

    public void addLast(T v) {
        Node<T> n = new Node<>(v);
        if (tail == null) head = tail = n;
        else { tail.next = n; tail = n; }
        size++;
    }

    /** Linear search from the head node: returns first match or null. */
    public T linearSearch(Predicate<T> match) {
        for (Node<T> c = head; c != null; c = c.next) if (match.test(c.data)) return c.data;
        return null;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            Node<T> cur = head;
            public boolean hasNext() { return cur != null; }
            public T next() {
                if (cur == null) throw new NoSuchElementException();
                T d = cur.data; cur = cur.next; return d;
            }
        };
    }
}
