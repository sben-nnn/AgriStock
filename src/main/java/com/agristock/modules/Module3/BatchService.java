package com.agristock.modules.Module3;

import com.agristock.ds.DynamicArray;
import com.agristock.model.Models.Batch;
import com.agristock.model.Models.Crop;
import com.agristock.model.Models.Farm;
import com.agristock.service.AgriStockSystem;

import java.time.LocalDate;

// Service for managing farms, crops and harvest batches. Delegates to the shared system
// structures (linked lists for farms/crops, AVL tree for batches).
public class BatchService {
    private final AgriStockSystem sys;

    public BatchService(AgriStockSystem sys) { this.sys = sys; }

    public void registerFarm(String id, String name, String location, double size) {
        sys.registerFarm(id, name, location, size);
    }
    public void registerCrop(String name, String variety, String category) {
        sys.registerCrop(name, variety, category);
    }
    public void createBatch(String id, String farmId, String crop, String variety, String worker,
                            LocalDate harvest, LocalDate expiry, double qty, String unit) {
        sys.createHarvestBatch(id, farmId, "", crop, variety, worker, "", harvest, expiry, qty, unit);
    }

    public void editFarm(String id, String name, String location, double size) { sys.editFarm(id, name, location, size); }
    public void deleteFarm(String id) { sys.deleteFarm(id); }
    public void editCrop(String name, String variety, String category, String newName, String newVariety, String newCategory) {
        sys.editCrop(name, variety, category, newName, newVariety, newCategory);
    }
    public void deleteCrop(String name, String variety) { sys.deleteCrop(name, variety); }
    public void editBatch(String batchId, String farmId, String crop, String variety, String worker,
                          LocalDate harvest, LocalDate expiry, double qty, String unit) {
        sys.editBatch(batchId, farmId, crop, variety, worker, harvest, expiry, qty, unit);
    }
    public void deleteBatch(String batchId) { sys.deleteBatch(batchId); }

    public String nextFarmId() { return sys.nextFarmId(); }
    public String nextBatchId() { return sys.nextBatchId(); }

    public DynamicArray<Farm> getAllFarms() {
        DynamicArray<Farm> out = new DynamicArray<>();
        for (Farm f : sys.farms) out.add(f);
        return out;
    }
    public DynamicArray<Crop> getAllCrops() {
        DynamicArray<Crop> out = new DynamicArray<>();
        for (Crop c : sys.crops) out.add(c);
        return out;
    }
    public DynamicArray<Batch> getAllBatches() { return sys.batches.toSortedArray(); }

    public Batch findById(String batchId) { return sys.batches.search(batchId == null ? "" : batchId); }
}
