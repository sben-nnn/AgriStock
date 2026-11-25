package com.agristock.modules.Module9;

import com.agristock.ds.DynamicArray;
import com.agristock.model.Models.Sale;
import com.agristock.model.Models.StockItem;
import com.agristock.service.AgriStockSystem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Service for managing sales. Delegates to the shared system structures
// (FIFO queue of pending orders and a linked list of confirmed records).
public class SalesService {
    private final AgriStockSystem sys;

    public SalesService(AgriStockSystem sys) { this.sys = sys; }

    public Sale create(String buyer, String itemId, double qty, double unitPrice) {
        return sys.createSale(buyer, itemId, qty, unitPrice);
    }

    public int processPending() { return sys.processPendingSales(); }

    public boolean hasPending() { return !sys.pendingSales.isEmpty(); }

    public LocalDate today() { return sys.today(); }

    public DynamicArray<StockItem> allItems() { return sys.traverseInventory(); }

    public List<Sale> pending() {
        List<Sale> out = new ArrayList<>();
        for (Sale s : sys.pendingSales) out.add(s);
        return out;
    }

    public List<Sale> records() {
        List<Sale> out = new ArrayList<>();
        for (Sale s : sys.sales) out.add(s);
        return out;
    }

    public Sale findById(String ref) {
        for (Sale s : sys.sales) if (s.ref.equals(ref)) return s;
        return null;
    }

    public void confirm(String ref) {
        Sale s = findById(ref);
        if (s != null) s.status = "CONFIRMED";
    }
}
