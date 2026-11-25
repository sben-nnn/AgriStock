package com.agristock.modules.Module4;

import com.agristock.ds.DynamicArray;
import com.agristock.model.Models.Batch;
import com.agristock.model.Models.StockRecord;
import com.agristock.service.AgriStockSystem;

import java.util.ArrayList;
import java.util.List;

// Service for managing harvest stock records. Delegates to the shared system
// structures (singly linked list with linear search).
public class HarvestStockService {
    private final AgriStockSystem sys;

    public HarvestStockService(AgriStockSystem sys) { this.sys = sys; }

    public StockRecord record(String batchId, double harvested, double rejected, String grade,
                              String ripeness, String rejectReason) {
        return sys.recordHarvestedStock(batchId, harvested, rejected, grade, ripeness, rejectReason);
    }

    public void edit(String batchId, double harvested, double rejected, String grade,
                     String ripeness, String rejectReason) {
        sys.editStockRecord(batchId, harvested, rejected, grade, ripeness, rejectReason);
    }

    public void delete(String batchId) { sys.deleteStockRecord(batchId); }

    public StockRecord findByBatch(String batchId) { return sys.findStockRecord(batchId); }

    public DynamicArray<StockRecord> getAll() {
        DynamicArray<StockRecord> out = new DynamicArray<>();
        for (StockRecord r : sys.stockRecords) out.add(r);
        return out;
    }

    /** Batches that still need a harvest stock record. */
    public List<String> eligibleBatchIds() {
        List<String> ids = new ArrayList<>();
        for (Batch b : sys.batches.toSortedArray())
            if (sys.findStockRecord(b.batchId) == null) ids.add(b.batchId);
        return ids;
    }
}
