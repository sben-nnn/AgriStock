package com.agristock.ds;

import java.util.function.Consumer;

/** Self-balancing AVL tree keyed by K. Used for users (M1), batches (M3) and stock (M7). */
public class AVLTree<K extends Comparable<K>, V> {
    private class Node {
        K key; V value; int height = 1; Node left, right;
        Node(K k, V v) { key = k; value = v; }
    }

    private Node root;
    private int size;
    private boolean inserted;

    public int size() { return size; }
    public int height() { return h(root); }

    /** AVL insert. Returns false (and changes nothing) if the key already exists. */
    public boolean insert(K key, V value) {
        inserted = false;
        root = insert(root, key, value);
        if (inserted) size++;
        return inserted;
    }

    private Node insert(Node n, K key, V value) {
        if (n == null) { inserted = true; return new Node(key, value); }
        int c = key.compareTo(n.key);
        if (c == 0) return n;                       // duplicate
        if (c < 0) n.left = insert(n.left, key, value);
        else n.right = insert(n.right, key, value);
        n.height = 1 + Math.max(h(n.left), h(n.right));
        int balance = h(n.left) - h(n.right);
        if (balance > 1 && balance(n.left) >= 0) return rotateRight(n);          // Left-Left
        if (balance < -1 && balance(n.right) <= 0) return rotateLeft(n);         // Right-Right
        if (balance > 1) { n.left = rotateLeft(n.left); return rotateRight(n); } // Left-Right
        if (balance < -1) { n.right = rotateRight(n.right); return rotateLeft(n); } // Right-Left
        return n;
    }

    /** Remove the node with the given key and rebalance. Returns true if a node was removed. */
    public boolean remove(K key) {
        boolean[] removed = { false };
        root = remove(root, key, removed);
        if (removed[0]) size--;
        return removed[0];
    }

    private Node remove(Node n, K key, boolean[] removed) {
        if (n == null) return null;
        int c = key.compareTo(n.key);
        if (c < 0) n.left = remove(n.left, key, removed);
        else if (c > 0) n.right = remove(n.right, key, removed);
        else {
            removed[0] = true;
            if (n.left == null) return n.right;
            if (n.right == null) return n.left;
            Node min = minNode(n.right);
            n.key = min.key; n.value = min.value;
            n.right = remove(n.right, n.key, removed);
        }
        n.height = 1 + Math.max(h(n.left), h(n.right));
        int balance = h(n.left) - h(n.right);
        if (balance > 1) {
            if (balance(n.left) < 0) n.left = rotateLeft(n.left);
            return rotateRight(n);
        }
        if (balance < -1) {
            if (balance(n.right) > 0) n.right = rotateRight(n.right);
            return rotateLeft(n);
        }
        return n;
    }

    private Node minNode(Node n) { while (n.left != null) n = n.left; return n; }

    /** Iterative binary search: O(log n). Returns null if not found. */
    public V search(K key) {
        Node cur = root;
        while (cur != null) {
            int c = key.compareTo(cur.key);
            if (c == 0) return cur.value;
            cur = c < 0 ? cur.left : cur.right;
        }
        return null;
    }

    /** In-order traversal: visits values in ascending key order. */
    public void inOrder(Consumer<V> visitor) { inOrder(root, visitor); }

    private void inOrder(Node n, Consumer<V> visitor) {
        if (n == null) return;
        inOrder(n.left, visitor);
        visitor.accept(n.value);
        inOrder(n.right, visitor);
    }

    public DynamicArray<V> toSortedArray() {
        DynamicArray<V> out = new DynamicArray<>();
        inOrder(out::add);
        return out;
    }

    private int h(Node n) { return n == null ? 0 : n.height; }
    private int balance(Node n) { return n == null ? 0 : h(n.left) - h(n.right); }

    private Node rotateRight(Node y) {
        Node x = y.left, t = x.right;
        x.right = y; y.left = t;
        y.height = 1 + Math.max(h(y.left), h(y.right));
        x.height = 1 + Math.max(h(x.left), h(x.right));
        return x;
    }

    private Node rotateLeft(Node x) {
        Node y = x.right, t = y.left;
        y.left = x; x.right = t;
        x.height = 1 + Math.max(h(x.left), h(x.right));
        y.height = 1 + Math.max(h(y.left), h(y.right));
        return y;
    }
}
