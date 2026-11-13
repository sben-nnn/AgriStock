package com.agristock.modules.Module2;

import com.agristock.model.Models.Farmer;
import com.agristock.ds.DynamicArray;

// Service for managing farmers
public class FarmerService {
    private DynamicArray<Farmer> farmers = new DynamicArray<>();

    // Add a farmer
    public void add(Farmer farmer) {
        farmers.add(farmer);
    }

    // Get all farmers
    public DynamicArray<Farmer> getAll() {
        return farmers;
    }

    // Find farmer by id
    public Farmer findById(String id) {
        for (int i = 0; i < farmers.size(); i++) {
            if (farmers.get(i).id.equalsIgnoreCase(id)) {
                return farmers.get(i);
            }
        }
        return null;
    }
}