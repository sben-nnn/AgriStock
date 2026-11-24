package com.agristock.test;

import com.agristock.model.Models.*;
import com.agristock.service.AgriStockSystem;

import java.time.LocalDate;

/**
 * Hard-coded data for testing modules 2-5 on their own
 * (Farmer Tracking, Farm/Crop/Batch Registration, Harvest Stock Recording, Quality Inspection).
 *
 * Everything is inserted straight into the system's data structures, so nothing depends on any
 * other module having been run first. All dates are relative to today.
 *
 * FARMERS (module 2)   FR-1001 Juan Dela Cruz, FR-1002 Maria Santos, FR-1003 Pedro Reyes - each with harvest logs.
 * FARMS / CROPS (3)    FARM-1001 Sunrise Farm, FARM-1002 Santos Orchard; Rice, Tomato, Mango, Corn, Eggplant, Banana.
 *                      The next generated IDs will be FARM-1003 and B-1009.
 *
 * BATCHES (module 3) and where each one stands:
 *   B-1001 Rice     already inspected - ACCEPTED, grade A      (in Stock, 950 kg)
 *   B-1002 Tomato   stock recorded, waiting in inspection queue - position 1 (front)
 *   B-1003 Mango    stock recorded, waiting in inspection queue - position 2
 *   B-1004 Corn     stock recorded, waiting in inspection queue - position 3
 *   B-1005 Eggplant already inspected - DOWNGRADED, grade B    (in Stock, 120 kg)
 *   B-1006 Banana   registered only - NO stock record yet      -> use it in module 4 (200 kg registered)
 *   B-1007 Tomato   registered only - NO stock record yet      -> use it in module 4 (80 kg registered)
 *   B-1008 Eggplant already inspected - REJECTED               (not in Stock)
 *
 * Module 4 has 6 stock records (B-1001..B-1005 and B-1008); module 5 has a 3-batch FIFO queue plus 3 inspected batches.
 */
public final class TestData2to5 {
    private TestData2to5() {}

    public static void load(AgriStockSystem sys) {
        sys.seedDefaultUsers();
        LocalDate t = sys.today();

        // ---- module 2: farmers + harvest logs (newest log first, as the app adds them)
        Farmer f1 = new Farmer("FR-1001", "Juan Dela Cruz", "0917-111-2222");
        f1.harvests.addFirst(new HarvestLog("Rice", 1000, t.minusDays(30)));
        f1.harvests.addFirst(new HarvestLog("Tomato", 300, t.minusDays(6)));
        Farmer f2 = new Farmer("FR-1002", "Maria Santos", "0918-333-4444");
        f2.harvests.addFirst(new HarvestLog("Mango", 250, t.minusDays(8)));
        f2.harvests.addFirst(new HarvestLog("Banana", 200, t.minusDays(3)));
        Farmer f3 = new Farmer("FR-1003", "Pedro Reyes", "0919-555-6666");
        f3.harvests.addFirst(new HarvestLog("Corn", 1500, t.minusDays(15)));
        sys.farmers.addFirst(f1);
        sys.farmers.addFirst(f2);
        sys.farmers.addFirst(f3);

        // ---- module 3: farms and crops
        sys.farms.addFirst(new Farm("FARM-1001", "Sunrise Farm", "Nueva Ecija", 3.5));
        sys.farms.addFirst(new Farm("FARM-1002", "Santos Orchard", "Batangas", 1.8));
        sys.crops.addFirst(new Crop("Rice", "Jasmine", "Grain"));
        sys.crops.addFirst(new Crop("Tomato", "Diamante", "Vegetable"));
        sys.crops.addFirst(new Crop("Mango", "Carabao", "Fruit"));
        sys.crops.addFirst(new Crop("Corn", "Yellow", "Grain"));
        sys.crops.addFirst(new Crop("Eggplant", "Long Purple", "Vegetable"));
        sys.crops.addFirst(new Crop("Banana", "Lakatan", "Fruit"));

        // ---- module 3: batches (the AVL tree is keyed by batch ID)
        Batch b1 = batch("B-1001", "FARM-1001", "Plot 1", "Rice", "Jasmine", "Ramon", "FR-1001", "sack (50 kg)", 20, 1000,
                t.minusDays(30), t.plusDays(150), BatchStatus.ACCEPTED, "A");
        Batch b2 = batch("B-1002", "FARM-1001", "Plot 2", "Tomato", "Diamante", "Liza", "FR-1001", "kg", 300, 300,
                t.minusDays(6), t.plusDays(4), BatchStatus.AWAITING_INSPECTION, "A");
        Batch b3 = batch("B-1003", "FARM-1002", "Block 4", "Mango", "Carabao", "Ben", "FR-1002", "kg", 250, 250,
                t.minusDays(8), t.plusDays(9), BatchStatus.AWAITING_INSPECTION, "B");
        Batch b4 = batch("B-1004", "FARM-1001", "Plot 3", "Corn", "Yellow", "Ramon", "FR-1003", "ton", 1.5, 1500,
                t.minusDays(15), t.plusDays(100), BatchStatus.AWAITING_INSPECTION, "A");
        Batch b5 = batch("B-1005", "FARM-1002", "Block 1", "Eggplant", "Long Purple", "Ben", "FR-1002", "kg", 120, 120,
                t.minusDays(20), t.plusDays(10), BatchStatus.DOWNGRADED, "B");
        Batch b6 = batch("B-1006", "FARM-1002", "Block 2", "Banana", "Lakatan", "Liza", "FR-1002", "kg", 200, 200,
                t.minusDays(3), t.plusDays(8), BatchStatus.REGISTERED, "");
        Batch b7 = batch("B-1007", "FARM-1001", "Plot 4", "Tomato", "Diamante", "Liza", "FR-1001", "kg", 80, 80,
                t.minusDays(1), t.plusDays(6), BatchStatus.REGISTERED, "");
        Batch b8 = batch("B-1008", "FARM-1002", "Block 3", "Eggplant", "Long Purple", "Ben", "FR-1002", "kg", 50, 50,
                t.minusDays(12), t.plusDays(1), BatchStatus.REJECTED, "C");
        for (Batch b : new Batch[]{b1, b2, b3, b4, b5, b6, b7, b8}) sys.batches.insert(b.batchId, b);

        // ---- module 4: harvest stock records (newest first, so add oldest first)
        StockRecord r1 = record("B-1001", 1000, 50, "A", "N/A", "Broken grains", t.minusDays(29));
        StockRecord r5 = record("B-1005", 120, 0, "B", "Overripe", "", t.minusDays(19));
        StockRecord r8 = record("B-1008", 50, 50, "C", "Overripe", "Contaminated", t.minusDays(11));   // rejected in inspection
        StockRecord r3 = record("B-1003", 250, 0, "B", "Ripe", "", t.minusDays(7));
        StockRecord r4 = record("B-1004", 1500, 30, "A", "N/A", "Moldy cobs", t.minusDays(2));
        StockRecord r2 = record("B-1002", 300, 15, "A", "Ripe", "Bruised fruit", t.minusDays(1));
        for (StockRecord r : new StockRecord[]{r1, r5, r8, r3, r4, r2}) sys.stockRecords.addFirst(r);

        // ---- module 5: inspection FIFO queue (front = B-1002)
        sys.inspectionQueue.enqueue(b2);
        sys.inspectionQueue.enqueue(b3);
        sys.inspectionQueue.enqueue(b4);

        // ---- stock for batches that already passed inspection (what module 5 would have created)
        addStock(sys, b1, r1, 60);
        addStock(sys, b5, r5, 40);
    }

    // ------------------------------------------------------------------ helpers

    private static Batch batch(String id, String farmId, String plot, String crop, String variety, String worker,
                               String farmerId, String unit, double originalQty, double qtyKg,
                               LocalDate harvest, LocalDate expiry, BatchStatus status, String grade) {
        Batch b = new Batch();
        b.batchId = id; b.farmId = farmId; b.plot = plot; b.crop = crop; b.variety = variety;
        b.worker = worker; b.farmerId = farmerId; b.unit = unit;
        b.originalQty = originalQty; b.quantityKg = qtyKg;
        b.harvestDate = harvest; b.expiryDate = expiry; b.status = status; b.grade = grade;
        return b;
    }

    private static StockRecord record(String batchId, double harvested, double rejected, String grade,
                                      String ripeness, String reason, LocalDate recorded) {
        StockRecord r = new StockRecord();
        r.batchId = batchId; r.harvestedQty = harvested; r.rejectedQty = rejected; r.saleableQty = harvested - rejected;
        r.grade = grade; r.ripeness = ripeness; r.rejectReason = reason; r.recorded = recorded;
        return r;
    }

    private static void addStock(AgriStockSystem sys, Batch b, StockRecord r, double unitValue) {
        StockItem s = new StockItem();
        s.itemId = b.batchId; s.crop = b.crop; s.category = sys.categoryOf(b.crop); s.grade = b.grade;
        s.harvestDate = b.harvestDate; s.expiryDate = b.expiryDate;
        s.harvested = r.harvestedQty; s.damaged = r.rejectedQty; s.remaining = r.saleableQty; s.unitValue = unitValue;
        sys.stock.insert(s.itemId, s);
        if (s.totalStock() > 0) sys.expiryHeap.insert(s);
    }
}