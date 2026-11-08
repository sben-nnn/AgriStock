package com.agristock.ds;

import java.util.Comparator;
import java.util.function.ToLongFunction;

/** Array algorithms for Reports & Analytics (M13): merge sort and binary search. */
public final class Sorts {
    private Sorts() {}

    public static <T> void mergeSort(DynamicArray<T> a, Comparator<T> c) {
        if (a.size() > 1) mergeSort(a, 0, a.size() - 1, c);
    }

    private static <T> void mergeSort(DynamicArray<T> a, int left, int right, Comparator<T> c) {
        if (left >= right) return;
        int mid = left + (right - left) / 2;
        mergeSort(a, left, mid, c);
        mergeSort(a, mid + 1, right, c);
        merge(a, left, mid, right, c);
    }

    @SuppressWarnings("unchecked")
    private static <T> void merge(DynamicArray<T> a, int left, int mid, int right, Comparator<T> c) {
        int n1 = mid - left + 1, n2 = right - mid;
        Object[] L = new Object[n1], R = new Object[n2];
        for (int i = 0; i < n1; i++) L[i] = a.get(left + i);
        for (int j = 0; j < n2; j++) R[j] = a.get(mid + 1 + j);
        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (c.compare((T) L[i], (T) R[j]) <= 0) a.set(k++, (T) L[i++]);
            else a.set(k++, (T) R[j++]);
        }
        while (i < n1) a.set(k++, (T) L[i++]);
        while (j < n2) a.set(k++, (T) R[j++]);
    }

    /** First index whose key is >= target in an array sorted ascending by key (or size() if none). */
    public static <T> int lowerBound(DynamicArray<T> a, long target, ToLongFunction<T> key) {
        int low = 0, high = a.size() - 1, result = a.size();
        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (key.applyAsLong(a.get(mid)) >= target) { result = mid; high = mid - 1; }
            else low = mid + 1;
        }
        return result;
    }

    /** First index whose key is > target (end boundary of a date range). */
    public static <T> int upperBound(DynamicArray<T> a, long target, ToLongFunction<T> key) {
        return lowerBound(a, target + 1, key);
    }
}
