package com.agristock.modules.Module10;

import com.agristock.model.Models.Delivery;
import com.agristock.ds.DynamicArray;
import java.time.LocalDate;

// Service for managing deliveries
public class DispatchService {
    private DynamicArray<Delivery> deliveries = new DynamicArray<>();

    // Add a delivery
    public void add(Delivery delivery) {
        deliveries.add(delivery);
    }

    // Get all deliveries
    public DynamicArray<Delivery> getAll() {
        return deliveries;
    }

    // Find delivery by id
    public Delivery findById(String id) {
        for (int i = 0; i < deliveries.size(); i++) {
            if (deliveries.get(i).id.equals(id)) {
                return deliveries.get(i);
            }
        }
        return null;
    }

    // Mark a delivery as dispatched today
    public void dispatch(String id) {
        Delivery d = findById(id);
        if (d != null) {
            d.status = "DISPATCHED";
            d.dispatchDate = LocalDate.now();
        }
    }
}
