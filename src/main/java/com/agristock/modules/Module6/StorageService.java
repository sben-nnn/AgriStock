package com.agristock.modules.Module6;

import com.agristock.ds.DynamicArray;
import com.agristock.model.Models.StorageEntry;
import com.agristock.model.Models.StockItem;
import com.agristock.service.AgriStockSystem;

import java.time.LocalDate;

// Service for managing storage entries. Delegates to the shared system structures
// (array of locations and a FIFO rotation queue).
public class StorageService {
    private final AgriStockSystem sys;

    public StorageService(AgriStockSystem sys) { this.sys = sys; }

    public StorageEntry assign(String itemId, int locationIndex, double qty, LocalDate date, String conditions) {
        return sys.assignLocation(itemId, locationIndex, qty, date, conditions);
    }

    public void edit(StorageEntry e, int locationIndex, double qty, LocalDate date, String conditions) {
        sys.editStorageEntry(e, locationIndex, qty, date, conditions);
    }

    public void delete(StorageEntry e) { sys.deleteStorageEntry(e); }

    public StorageEntry rotateFIFO() { return sys.rotateStockFIFO(); }

    public DynamicArray<StorageEntry> getAll() {
        DynamicArray<StorageEntry> out = new DynamicArray<>();
        for (StorageEntry e : sys.rotationQueue) out.add(e);
        return out;
    }

    public DynamicArray<StockItem> allItems() { return sys.traverseInventory(); }

    /** Items that still have unstored stock available. */
    public DynamicArray<StockItem> eligibleItems() {
        DynamicArray<StockItem> out = new DynamicArray<>();
        for (StockItem s : sys.traverseInventory())
            if (s.remaining > 0.0001) out.add(s);
        return out;
    }

    public String[] locations() { return sys.locations; }
}
