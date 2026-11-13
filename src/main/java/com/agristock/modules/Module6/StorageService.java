package com.agristock.modules.Module6;

import com.agristock.model.Models.StorageEntry;
import com.agristock.ds.DynamicArray;

// Service for managing storage entries
public class StorageService {
    private DynamicArray<StorageEntry> entries = new DynamicArray<>();

    // Add a storage entry
    public void add(StorageEntry entry) {
        entries.add(entry);
    }

    // Get all storage entries
    public DynamicArray<StorageEntry> getAll() {
        return entries;
    }

    // Find entry by batch id
    public StorageEntry findByBatch(String batchId) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).batchId.equals(batchId)) {
                return entries.get(i);
            }
        }
        return null;
    }
}
