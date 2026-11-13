package com.agristock.modules.Module5;

import com.agristock.model.Models.Batch;
import com.agristock.model.Models.BatchStatus;
import com.agristock.ds.DynamicArray;

// Service for managing batch inspections
public class InspectionService {
    private DynamicArray<Batch> queue = new DynamicArray<>();

    // Add a batch to inspection
    public void add(Batch batch) {
        queue.add(batch);
    }

    // Get all batches in inspection
    public DynamicArray<Batch> getAll() {
        return queue;
    }

    // Find batch by id
    public Batch findById(String batchId) {
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).batchId.equalsIgnoreCase(batchId)) {
                return queue.get(i);
            }
        }
        return null;
    }

    // Update batch inspection status
    public void inspect(String batchId, BatchStatus status) {
        Batch b = findById(batchId);
        if (b != null) {
            b.status = status;
        }
    }
}