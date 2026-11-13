package com.agristock.modules.Module8;

import com.agristock.model.Models.StockItem;
import com.agristock.model.Models.AgriException;
import com.agristock.ds.DynamicArray;
import java.time.LocalDate;

// Service for checking expiry dates
public class ExpiryService {
    private DynamicArray<StockItem> items = new DynamicArray<>();

    // Add a stock item
    public void add(StockItem item) {
        items.add(item);
    }

    // Get items expiring before a given date
    public DynamicArray<StockItem> expiringBefore(LocalDate date) {
        DynamicArray<StockItem> result = new DynamicArray<>();
        for (int i = 0; i < items.size(); i++) {
            StockItem item = items.get(i);
            if (item.expiryDate != null && item.expiryDate.isBefore(date)) {
                result.add(item);
            }
        }
        return result;
    }
}
