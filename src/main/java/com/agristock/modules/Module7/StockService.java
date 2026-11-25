package com.agristock.modules.Module7;

import com.agristock.ds.DynamicArray;
import com.agristock.model.Models.Crop;
import com.agristock.model.Models.StockItem;
import com.agristock.service.AgriStockSystem;

import java.time.LocalDate;

// Service for managing stock items. Delegates to the shared system structures
// (AVL tree keyed by item id, with binary search and in-order traversal).
public class StockService {
    private final AgriStockSystem sys;

    public StockService(AgriStockSystem sys) { this.sys = sys; }

    public StockItem add(String crop, String category, double qty, LocalDate expiry, double unitValue) {
        return sys.addItem(crop, category, qty, expiry, unitValue);
    }

    public void edit(String itemId, String category, LocalDate expiry, double unitValue) {
        sys.editItem(itemId, category, expiry, unitValue);
    }

    public void delete(String itemId) { sys.deleteItem(itemId); }

    public void reserve(String itemId, double qty) { sys.reserveManually(itemId, qty); }
    public void release(String itemId, double qty) { sys.releaseManually(itemId, qty); }

    public StockItem find(String itemId) { return sys.searchStock(itemId); }

    public DynamicArray<StockItem> getAll() { return sys.traverseInventory(); }

    public DynamicArray<StockItem> filter(String text, String category, String flag) {
        return sys.filterStock(text, category, flag);
    }

    public String flagsOf(StockItem s) { return sys.flagsOf(s); }

    public java.util.List<String> cropNames() {
        java.util.List<String> names = new java.util.ArrayList<>();
        for (Crop c : sys.crops) names.add(c.name);
        return names;
    }
}
