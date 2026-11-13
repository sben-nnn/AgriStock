package com.agristock.modules.Module3;

import com.agristock.model.Models.Batch;
import com.agristock.ds.DynamicArray;

// Service for managing harvest batches
public class BatchService {
    private DynamicArray<Batch> batches = new DynamicArray<>();

    // Add a batch
    public void add(Batch batch) {
        batches.add(batch);
    }

    // Get all batches
    public DynamicArray<Batch> getAll() {
        return batches;
    }

    // Find batch by id
    public Batch findById(String batchId) {
        for (int i = 0; i < batches.size(); i++) {
            if (batches.get(i).batchId.equalsIgnoreCase(batchId)) {
                return batches.get(i);
            }
        }
        return null;
    }
}