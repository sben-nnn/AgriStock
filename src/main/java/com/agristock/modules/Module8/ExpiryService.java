package com.agristock.modules.Module8;

import com.agristock.ds.DynamicArray;
import com.agristock.model.Models.StockItem;
import com.agristock.service.AgriStockSystem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Service for checking expiry and FEFO dispatch. Delegates to the shared system
// structures (min-heap ordered by expiry date).
public class ExpiryService {
    private final AgriStockSystem sys;

    public ExpiryService(AgriStockSystem sys) { this.sys = sys; }

    public List<String> dispatchFEFO(String crop, double qty) { return sys.dispatchFEFO(crop, qty); }
    public List<com.agristock.model.Models.Spoilage> purgeExpired() { return sys.checkAndPurgeExpired(); }
    public void rebuildHeap() { sys.rebuildExpiryHeap(); }

    public boolean isExpired(StockItem s) { return sys.flagExpired(s); }
    public boolean isNearExpiry(StockItem s) { return sys.flagNearExpiry(s); }
    public LocalDate today() { return sys.today(); }
    public StockItem find(String itemId) { return sys.searchStock(itemId); }

    public DynamicArray<StockItem> allItems() { return sys.traverseInventory(); }

    /** Items with stock on hand, sorted by expiry date (earliest first). */
    public List<StockItem> byExpiry() {
        List<StockItem> out = new ArrayList<>();
        for (StockItem s : sys.traverseInventory())
            if (s.totalStock() > 0) out.add(s);
        out.sort(Comparator.comparing(s -> s.expiryDate == null ? LocalDate.MAX : s.expiryDate));
        return out;
    }

    public List<StockItem> expired() {
        List<StockItem> out = new ArrayList<>();
        for (StockItem s : sys.traverseInventory())
            if (s.totalStock() > 0 && sys.flagExpired(s)) out.add(s);
        return out;
    }

    public List<String> cropNames() {
        List<String> crops = new ArrayList<>();
        crops.add("Any crop");
        for (StockItem s : sys.traverseInventory())
            if (!crops.contains(s.crop)) crops.add(s.crop);
        return crops;
    }
}
