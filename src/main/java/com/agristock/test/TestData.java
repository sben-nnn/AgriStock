package com.agristock.test;

import com.agristock.model.Models.*;
import com.agristock.service.AgriStockSystem;

import java.time.LocalDate;

/**
 * Hard-coded data for testing modules 11-14 on their own.
 *
 * Everything is inserted straight into the system's data structures, so nothing here depends on
 * modules 3-10 (batches, inspection, storage, sales, dispatch) being run first.
 *
 * The data matches the state of TEST_MANUAL.md right after Step 10, so the expected results
 * written in the manual for Steps 11-14 apply directly:
 *   - B-1001 Rice   : 1000 harvested, 50 rejected, 200 sold -> 750 remaining @ 60.00
 *   - B-1002 Tomato : 300 harvested, 80 sold, 12 spoiled     -> 208 remaining @ 90.00
 *   - M-2001 Corn   : 100 remaining @ 25.00
 *   - SO-0001 / SO-0002 confirmed; DL-0001 / DL-0002 received (DL-0001 short by 10 kg)
 *   - SP-0001 (delivery shortage, 10 kg) and SP-0002 (spoilage, 12 kg) approved
 *   - Rice yield history: 2024 Dry, 2024 Wet, 2025 Dry
 *
 * Extras on top of the manual: one PENDING spoilage request (SP-0003), five seasons of Tomato
 * history, and a few audit-trail rows.
 */
public final class TestData {
    private TestData() {}

    public static void load(AgriStockSystem sys) {
        sys.seedDefaultUsers();
        LocalDate today = sys.today();
        LocalDate harvest1 = LocalDate.of(2026, 11, 1);
        LocalDate harvest2 = LocalDate.of(2026, 11, 10);

        // ---- farm + crops (needed by the Reports filters and farm-name lookup)
        sys.farms.addFirst(new Farm("FARM-1001", "Sunrise Farm", "Nueva Ecija", 3.5));
        sys.crops.addFirst(new Crop("Rice", "Jasmine", "Grain"));
        sys.crops.addFirst(new Crop("Tomato", "Diamante", "Vegetable"));
        sys.crops.addFirst(new Crop("Corn", "Yellow", "Grain"));

        // ---- batches (used by the Harvest report)
        sys.batches.insert("B-1001", batch("B-1001", "Rice", "Jasmine", "Plot 1", "Ramon", 1000, harvest1, LocalDate.of(2035, 12, 31)));
        sys.batches.insert("B-1002", batch("B-1002", "Tomato", "Diamante", "Plot 2", "Liza", 300, harvest2, LocalDate.of(2034, 12, 31)));

        // ---- stock items (balances are AFTER the sales and the spoilage entries below)
        addStock(sys, "B-1001", "Rice", "Grain", "Cold Room A", harvest1, LocalDate.of(2035, 12, 31),
                1000, 50, 200, 900, 750, 60);
        addStock(sys, "B-1002", "Tomato", "Vegetable", "Cold Room B", harvest2, LocalDate.of(2034, 12, 31),
                300, 12, 80, 285, 208, 90);
        addStock(sys, "M-2001", "Corn", "Grain", "Unassigned", today, LocalDate.of(2035, 1, 1),
                100, 0, 0, 0, 100, 25);

        // ---- confirmed sales
        sys.sales.addFirst(sale("SO-0001", "Metro Market", "B-1001", "Rice", 200, 62, today));
        sys.sales.addFirst(sale("SO-0002", "Fresh Mart", "B-1002", "Tomato", 80, 95, today));
        sys.saleCounter = 2;

        // ---- deliveries (both already received; DL-0001 came up 10 kg short)
        sys.deliveries.addFirst(delivery("DL-0001", "SO-0001", "Metro Market", "B-1001", "Truck TRK-101",
                "Metro Market, Quezon City", 200, 190, today.plusDays(2), today, 1));
        sys.deliveries.addFirst(delivery("DL-0002", "SO-0002", "Fresh Mart", "B-1002", "Van VAN-205",
                "Fresh Mart, Manila", 80, 80, today.plusDays(5), today, 2));
        sys.deliveryCounter = 2;

        // ---- spoilage log (newest first, so add oldest first)
        sys.spoilage.addFirst(spoilage("SP-0001", "B-1001", 10, "Delivery shortage on DL-0001", "admin",
                "admin", "APPROVED", "Delivery shortage", 10 * 60.0, today));
        sys.spoilage.addFirst(spoilage("SP-0002", "B-1002", 12, "Spoiled during transport", "admin",
                "admin", "APPROVED", "Spoilage", 12 * 90.0, today));
        // Extra: a pending request, so Approve / Reject can be tested right away (loss reports ignore it until approved)
        sys.spoilage.addFirst(spoilage("SP-0003", "B-1001", 5, "Pest damage", "inventory",
                "", "PENDING", "Spoilage", 0, today));
        sys.spoilageCounter = 3;

        // ---- forecasting history
        double[][] rice = {{4200, 4000}, {4500, 4300}, {4100, 4400}};
        String[] riceSeasons = {"2024 Dry", "2024 Wet", "2025 Dry"};
        for (int i = 0; i < riceSeasons.length; i++)
            sys.yields.add(new YieldRecord("Rice", riceSeasons[i], rice[i][0], rice[i][1]));
        double[][] tomato = {{1200, 1100}, {1350, 1300}, {1100, 1250}, {1500, 1400}, {1450, 1500}};
        String[] tomatoSeasons = {"2024 Dry", "2024 Wet", "2025 Dry", "2025 Wet", "2026 Dry"};
        for (int i = 0; i < tomatoSeasons.length; i++)
            sys.yields.add(new YieldRecord("Tomato", tomatoSeasons[i], tomato[i][0], tomato[i][1]));

        // ---- a few audit-trail rows (written while no one is signed in, so the user shows as SYSTEM)
        sys.logAuditTrail("HARVEST", "B-1001", 1000, 0, "Batch registered: Rice (Jasmine)");
        sys.logAuditTrail("HARVEST", "B-1002", 300, 0, "Batch registered: Tomato (Diamante)");
        sys.logAuditTrail("SALE", "SO-0001", 200, 0, "Sale confirmed - stock reduced");
        sys.logAuditTrail("SALE", "SO-0002", 80, 0, "Sale confirmed - stock reduced");
        sys.logAuditTrail("DISPATCH", "DL-0001", 200, 190, "Receipt confirmed");
        sys.logAuditTrail("ADJUSTMENT", "SP-0002", 12, 0, "Adjustment approved by admin");
        sys.logAuditTrail("ADJUSTMENT", "SP-0003", 5, 0, "Spoilage reported: Pest damage");
    }

    // ------------------------------------------------------------------ helpers

    private static Batch batch(String id, String crop, String variety, String plot, String worker,
                               double qtyKg, LocalDate harvest, LocalDate expiry) {
        Batch b = new Batch();
        b.batchId = id; b.farmId = "FARM-1001"; b.plot = plot; b.crop = crop; b.variety = variety;
        b.worker = worker; b.farmerId = ""; b.unit = "kg"; b.grade = "A";
        b.originalQty = qtyKg; b.quantityKg = qtyKg;
        b.harvestDate = harvest; b.expiryDate = expiry;
        b.status = BatchStatus.ACCEPTED;
        return b;
    }

    private static void addStock(AgriStockSystem sys, String id, String crop, String category, String location,
                                 LocalDate harvest, LocalDate expiry, double harvested, double damaged,
                                 double sold, double stored, double remaining, double unitValue) {
        StockItem s = new StockItem();
        s.itemId = id; s.crop = crop; s.category = category; s.location = location; s.grade = "A";
        s.harvestDate = harvest; s.expiryDate = expiry;
        s.harvested = harvested; s.damaged = damaged; s.sold = sold; s.stored = stored;
        s.remaining = remaining; s.unitValue = unitValue;
        sys.stock.insert(id, s);
        if (s.remaining > 0) sys.expiryHeap.insert(s);
    }

    private static Sale sale(String ref, String buyer, String itemId, String crop, double qty, double price, LocalDate date) {
        Sale s = new Sale();
        s.ref = ref; s.buyer = buyer; s.itemId = itemId; s.crop = crop;
        s.qty = qty; s.unitPrice = price; s.date = date;
        s.status = "CONFIRMED"; s.deliveryScheduled = true;
        return s;
    }

    private static Delivery delivery(String id, String saleRef, String company, String itemId, String vehicle,
                                     String destination, double qty, double received, LocalDate deadline,
                                     LocalDate date, int urgency) {
        Delivery d = new Delivery();
        d.id = id; d.saleRef = saleRef; d.company = company; d.itemId = itemId; d.vehicle = vehicle;
        d.destination = destination; d.qty = qty; d.dispatchedQty = qty; d.receivedQty = received;
        d.deadline = deadline; d.dispatchDate = date; d.receivedDate = date; d.urgency = urgency;
        d.status = "RECEIVED";
        return d;
    }

    private static Spoilage spoilage(String id, String itemId, double qty, String reason, String reporter,
                                     String approver, String status, String kind, double lossValue, LocalDate date) {
        Spoilage sp = new Spoilage();
        sp.id = id; sp.itemId = itemId; sp.qty = qty; sp.reason = reason; sp.reporter = reporter;
        sp.approver = approver; sp.status = status; sp.kind = kind; sp.lossValue = lossValue; sp.date = date;
        return sp;
    }
}