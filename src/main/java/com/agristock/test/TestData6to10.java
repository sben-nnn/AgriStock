package com.agristock.test;

import com.agristock.model.Models.*;
import com.agristock.service.AgriStockSystem;

import java.time.LocalDate;

/**
 * Hard-coded data for testing modules 6-10 on their own (Storage, Stock, Expiry, Sales, Dispatch).
 *
 * Everything is inserted straight into the system's data structures, so nothing depends on
 * modules 1-5 (batch registration, harvest recording, inspection) having been run first.
 * All dates are relative to today, so flags such as Expired / Near-expiry stay correct on any day.
 *
 * STOCK (item - crop - remaining / stored / reserved - flag):
 *   B-1001 Rice     750 / 700 / 100 - Aging          (50 kg still unstored; 100 kg reserved by SO-0006)
 *   B-1002 Tomato   185 / 185 /   0 - Near-expiry    (fully stored; expires in 4 days)
 *   B-1003 Mango    200 /   0 /   0 - (none)         (nothing stored yet -> use it to test Assign Location)
 *   B-1004 Corn    1470 /1470 / 300 - Aging          (300 kg reserved by SO-0005)
 *   B-1005 Eggplant 120 /   0 /  30 - Expired, Aging (use it to test the purge; 30 kg reserved by SO-0007)
 *   B-1006 Banana    50 /  50 /   0 - Low            (available is 50 kg = low-stock threshold)
 *
 * STORAGE FIFO QUEUE (oldest first): B-1001, B-1004, B-1002, B-1006.
 *
 * SALES
 *   Confirmed : SO-0001 (B-1002, 100 kg), SO-0002 (B-1001, 200 kg), SO-0003 (B-1003, 50 kg), SO-0004 (B-1006, 20 kg)
 *   Pending   : SO-0005 (B-1004, 300 kg), SO-0006 (B-1001, 100 kg), SO-0007 (B-1005, 30 kg - batch is expired)
 *
 * DELIVERIES
 *   DL-0001 SCHEDULED (SO-0001, deadline today+3, urgency 2)
 *   DL-0002 SCHEDULED (SO-0002, deadline today+1, urgency 1)  <- earliest deadline, dispatched first
 *   DL-0003 DISPATCHED (SO-0003, 50 kg)                        <- ready for Confirm Receipt
 *   SO-0004 is confirmed and has NO delivery yet                <- ready for Schedule Delivery
 */
public final class TestData6to10 {
    private TestData6to10() {}

    public static void load(AgriStockSystem sys) {
        sys.seedDefaultUsers();
        LocalDate t = sys.today();

        // ---- stock items (balances already include the confirmed sales below)
        addStock(sys, "B-1001", "Rice", "Grain", "Dry Warehouse 1", t.minusDays(30), t.plusDays(150),
                1000, 50, 200, 700, 100, 50, 60);
        addStock(sys, "B-1002", "Tomato", "Vegetable", "Cold Room A", t.minusDays(6), t.plusDays(4),
                300, 15, 100, 185, 0, 0, 55);
        addStock(sys, "B-1003", "Mango", "Fruit", "Unassigned", t.minusDays(8), t.plusDays(9),
                250, 0, 50, 0, 0, 200, 70);
        addStock(sys, "B-1004", "Corn", "Grain", "Grain Silo 1", t.minusDays(15), t.plusDays(100),
                1500, 30, 0, 1470, 300, 0, 22);
        addStock(sys, "B-1005", "Eggplant", "Vegetable", "Unassigned", t.minusDays(20), t.minusDays(2),
                120, 0, 0, 0, 30, 120, 40);
        addStock(sys, "B-1006", "Banana", "Fruit", "Cold Room B", t.minusDays(3), t.plusDays(8),
                70, 0, 20, 50, 0, 0, 35);

        // ---- storage FIFO rotation queue (oldest lot first)
        sys.rotationQueue.enqueue(storage("B-1001", "Dry Warehouse 1", 700, t.minusDays(29), "22\u00B0C, 60% RH"));
        sys.rotationQueue.enqueue(storage("B-1004", "Grain Silo 1", 1470, t.minusDays(14), "Ambient, ventilated"));
        sys.rotationQueue.enqueue(storage("B-1002", "Cold Room A", 185, t.minusDays(5), "10\u00B0C, 90% RH"));
        sys.rotationQueue.enqueue(storage("B-1006", "Cold Room B", 50, t.minusDays(2), "13\u00B0C, 85% RH"));

        // ---- sales: records are kept newest first (addFirst), the pending queue is FIFO
        Sale so1 = sale("SO-0001", "Metro Market", "B-1002", "Tomato", 100, 60, "CONFIRMED", true, t);
        Sale so2 = sale("SO-0002", "Golden Grains Trading", "B-1001", "Rice", 200, 62, "CONFIRMED", true, t);
        Sale so3 = sale("SO-0003", "Fresh Mart", "B-1003", "Mango", 50, 95, "CONFIRMED", true, t);
        Sale so4 = sale("SO-0004", "Green Grocer", "B-1006", "Banana", 20, 50, "CONFIRMED", false, t);
        Sale so5 = sale("SO-0005", "Metro Market", "B-1004", "Corn", 300, 30, "PENDING", false, t);
        Sale so6 = sale("SO-0006", "Fresh Mart", "B-1001", "Rice", 100, 64, "PENDING", false, t);
        Sale so7 = sale("SO-0007", "Local Eatery", "B-1005", "Eggplant", 30, 45, "PENDING", false, t);
        for (Sale s : new Sale[]{so1, so2, so3, so4, so5, so6, so7}) sys.sales.addFirst(s);
        sys.pendingSales.enqueue(so5);
        sys.pendingSales.enqueue(so6);
        sys.pendingSales.enqueue(so7);
        sys.saleCounter = 7;

        // ---- deliveries
        Delivery dl1 = delivery("DL-0001", "SO-0001", "Metro Market", "B-1002", "Truck TRK-101",
                "Metro Market, Quezon City", 100, t.plusDays(3), 2, "SCHEDULED", null);
        Delivery dl2 = delivery("DL-0002", "SO-0002", "Golden Grains Trading", "B-1001", "Van VAN-205",
                "Golden Grains, Cabanatuan", 200, t.plusDays(1), 1, "SCHEDULED", null);
        Delivery dl3 = delivery("DL-0003", "SO-0003", "Fresh Mart", "B-1003", "Truck TRK-102",
                "Fresh Mart, Manila", 50, t, 2, "DISPATCHED", t);
        for (Delivery d : new Delivery[]{dl1, dl2, dl3}) sys.deliveries.addFirst(d);
        sys.deliveryQueue.enqueue(dl1);              // only scheduled deliveries wait in the priority queue
        sys.deliveryQueue.enqueue(dl2);
        sys.deliveryCounter = 3;
    }

    // ------------------------------------------------------------------ helpers

    private static void addStock(AgriStockSystem sys, String id, String crop, String category, String location,
                                 LocalDate harvest, LocalDate expiry, double harvested, double damaged, double sold,
                                 double stored, double reserved, double remaining, double unitValue) {
        StockItem s = new StockItem();
        s.itemId = id; s.crop = crop; s.category = category; s.location = location; s.grade = "A";
        s.harvestDate = harvest; s.expiryDate = expiry;
        s.harvested = harvested; s.damaged = damaged; s.sold = sold; s.stored = stored;
        s.reserved = reserved; s.remaining = remaining; s.unitValue = unitValue;
        sys.stock.insert(id, s);
        if (s.totalStock() > 0) sys.expiryHeap.insert(s);
    }

    private static StorageEntry storage(String batchId, String location, double qty, LocalDate date, String conditions) {
        StorageEntry e = new StorageEntry();
        e.batchId = batchId; e.location = location; e.qty = qty; e.storageDate = date;
        e.conditions = conditions; e.rotations = 0;
        return e;
    }

    private static Sale sale(String ref, String buyer, String itemId, String crop, double qty, double price,
                             String status, boolean deliveryScheduled, LocalDate date) {
        Sale s = new Sale();
        s.ref = ref; s.buyer = buyer; s.itemId = itemId; s.crop = crop;
        s.qty = qty; s.unitPrice = price; s.status = status;
        s.deliveryScheduled = deliveryScheduled; s.date = date;
        return s;
    }

    private static Delivery delivery(String id, String saleRef, String company, String itemId, String vehicle,
                                     String destination, double qty, LocalDate deadline, int urgency,
                                     String status, LocalDate dispatchDate) {
        Delivery d = new Delivery();
        d.id = id; d.saleRef = saleRef; d.company = company; d.itemId = itemId; d.vehicle = vehicle;
        d.destination = destination; d.qty = qty; d.deadline = deadline; d.urgency = urgency; d.status = status;
        if (status.equals("DISPATCHED")) { d.dispatchedQty = qty; d.dispatchDate = dispatchDate; }
        return d;
    }
}