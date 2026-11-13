package com.agristock.service;

import com.agristock.model.AgriException;

import java.time.LocalDate;

/** Loads demo data made for testing (the proposal states the system is tested with group-made sample data). */
public final class SampleData {
    private SampleData() {}

    public static void load(AgriStockSystem s) {
        if (s.batches.size() > 0 || s.farmers.size() > 0) throw new AgriException("Sample data can only be loaded into an empty system.");
        LocalDate t = s.today();
        s.registerFarmer("F-001", "Juan Dela Cruz", "0917-111-2222");
        s.registerFarmer("F-002", "Maria Santos", "0918-333-4444");
        s.registerFarmer("F-003", "Pedro Reyes", "0919-555-6666");
        s.registerFarm("FARM-A", "Dela Cruz Farm", "Nueva Ecija");
        s.registerFarm("FARM-B", "Santos Orchard", "Batangas");
        s.registerCrop("Rice", "Jasmine", "Grain");
        s.registerCrop("Tomato", "Diamante", "Vegetable");
        s.registerCrop("Mango", "Carabao", "Fruit");
        s.registerCrop("Corn", "Yellow", "Grain");
        s.registerCrop("Eggplant", "Long Purple", "Vegetable");
        s.registerCrop("Banana", "Lakatan", "Fruit");

        s.createHarvestBatch("B-1001", "FARM-A", "Plot 1", "Rice", "Jasmine", "Ramon", "F-001", t.minusDays(30), t.plusDays(150), 20, "sack (50 kg)");
        s.createHarvestBatch("B-1002", "FARM-A", "Plot 2", "Tomato", "Diamante", "Liza", "F-001", t.minusDays(6), t.plusDays(4), 300, "kg");
        s.createHarvestBatch("B-1003", "FARM-B", "Block 4", "Mango", "Carabao", "Ben", "F-002", t.minusDays(8), t.plusDays(9), 250, "kg");
        s.createHarvestBatch("B-1004", "FARM-A", "Plot 3", "Corn", "Yellow", "Ramon", "F-003", t.minusDays(15), t.plusDays(100), 1.5, "ton");
        s.createHarvestBatch("B-1005", "FARM-B", "Block 1", "Eggplant", "Long Purple", "Ben", "F-002", t.minusDays(20), t.minusDays(2), 120, "kg");
        s.createHarvestBatch("B-1006", "FARM-B", "Block 2", "Banana", "Lakatan", "Liza", "F-002", t.minusDays(3), t.plusDays(8), 200, "kg");
        s.recordHarvest("F-001", "Rice", 1000, t.minusDays(30));
        s.recordHarvest("F-001", "Tomato", 300, t.minusDays(6));
        s.recordHarvest("F-002", "Mango", 250, t.minusDays(8));

        s.recordHarvestedStock("B-1001", 1000, 50, "A", "N/A", 13.5, "Broken grains");
        s.recordHarvestedStock("B-1002", 300, 15, "A", "Ripe", 90, "Bruised fruit");
        s.recordHarvestedStock("B-1003", 250, 0, "B", "Ripe", 82, "");
        s.recordHarvestedStock("B-1004", 1500, 30, "A", "N/A", 14, "Moldy cobs");
        s.recordHarvestedStock("B-1005", 120, 0, "B", "Overripe", 91, "");
        s.recordHarvestedStock("B-1006", 200, 0, "A", "Unripe", 75, "");

        s.inspectBatch("ACCEPTED", "A", "", 48);
        s.inspectBatch("ACCEPTED", "A", "", 55);
        s.inspectBatch("DOWNGRADED", "B", "Minor blemishes, sold as grade B", 70);
        s.inspectBatch("ACCEPTED", "A", "", 22);
        s.inspectBatch("ACCEPTED", "B", "", 40);          // B-1006 stays in the inspection queue

        s.assignLocation("B-1001", 2, 900, t.minusDays(29), "22\u00B0C, 60% RH");
        s.assignLocation("B-1002", 0, 285, t.minusDays(5), "10\u00B0C, 90% RH");
        s.assignLocation("B-1003", 1, 250, t.minusDays(7), "13\u00B0C, 85% RH");
        s.assignLocation("B-1004", 4, 1470, t.minusDays(14), "Ambient, ventilated");

        s.createSale("Metro Market", "B-1002", 100, 60, "");
        s.createSale("Golden Grains Trading", "B-1001", 200, 62, "");
        s.createSale("Fresh Mart", "B-1003", 80, 95, "");
        s.confirmSale();
        s.confirmSale();
        s.scheduleDelivery(s.sales.linearSearch(x -> x.ref.equals("SO-0001")), t.plusDays(2), "Truck TRK-101", "Metro Market, Quezon City", 1);
        s.scheduleDelivery(s.sales.linearSearch(x -> x.ref.equals("SO-0002")), t.plusDays(5), "Van VAN-205", "Golden Grains, Cabanatuan", 2);

        s.recordSpoilage("B-1002", 12, "Spoiled during transport", "inventory");
        ReportService rs = new ReportService(s);
        double[][] rice = {{4200, 4000}, {4500, 4300}, {4100, 4400}, {4800, 4600}, {5000, 4900}};
        double[][] tomato = {{1200, 1100}, {1350, 1300}, {1100, 1250}, {1500, 1400}, {1450, 1500}};
        String[] seasons = {"2024 Dry", "2024 Wet", "2025 Dry", "2025 Wet", "2026 Dry"};
        for (int i = 0; i < seasons.length; i++) {
            rs.addYieldRecord("Rice", seasons[i], rice[i][0], rice[i][1]);
            rs.addYieldRecord("Tomato", seasons[i], tomato[i][0], tomato[i][1]);
        }
        s.logTransaction("SAMPLE_DATA", "-", "Sample data loaded");
    }
}
