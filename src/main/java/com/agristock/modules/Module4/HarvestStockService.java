package com.agristock.modules.Module4;

import com.agristock.model.Models.StockRecord;
import com.agristock.ds.DynamicArray;

// Service for managing harvest stock records
public class HarvestStockService {
    private DynamicArray<StockRecord> records = new DynamicArray<>();

    // Add a stock record
    public void add(StockRecord record) {
        records.add(record);
    }

    // Get all stock records
    public DynamicArray<StockRecord> getAll() {
        return records;
    }

    // Find stock record by batch id
    public StockRecord findByBatch(String batchId) {
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).batchId.equalsIgnoreCase(batchId)) {
                return records.get(i);
            }
        }
        return null;
    }
}