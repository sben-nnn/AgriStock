package com.agristock.modules.Module7;

import com.agristock.model.Models.StockItem;
import com.agristock.ds.DynamicArray;

// Service for managing stock items
public class StockService {
    private DynamicArray<StockItem> items = new DynamicArray<>();

    // Add a stock item
    public void add(StockItem item) {
        items.add(item);
    }

    // Get all stock items
    public DynamicArray<StockItem> getAll() {
        return items;
    }

    // Find item by id
    public StockItem findById(String itemId) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).itemId.equals(itemId)) {
                return items.get(i);
            }
        }
        return null;
    }
}
