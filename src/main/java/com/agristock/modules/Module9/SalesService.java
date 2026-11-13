package com.agristock.modules.Module9;

import com.agristock.model.Models.Sale;
import com.agristock.ds.DynamicArray;

// Service for managing sales
public class SalesService {
    private DynamicArray<Sale> sales = new DynamicArray<>();

    // Add a sale
    public void add(Sale sale) {
        sales.add(sale);
    }

    // Get all sales
    public DynamicArray<Sale> getAll() {
        return sales;
    }

    // Find sale by reference id
    public Sale findById(String ref) {
        for (int i = 0; i < sales.size(); i++) {
            if (sales.get(i).ref.equals(ref)) {
                return sales.get(i);
            }
        }
        return null;
    }

    // Mark a sale as confirmed
    public void confirm(String ref) {
        Sale s = findById(ref);
        if (s != null) {
            s.status = "CONFIRMED";
        }
    }
}
