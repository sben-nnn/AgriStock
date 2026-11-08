package com.agristock.service;

import com.agristock.ds.DynamicArray;
import com.agristock.ds.Sorts;
import com.agristock.model.AgriException;
import com.agristock.model.Models.*;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class ReportService {
    private final AgriStockSystem sys;
    public ReportService(AgriStockSystem sys) { this.sys = sys; }

    // ---------------------------------------------------------------- M12
    public void addYieldRecord(String crop, String season, double yield, double demand) {
        if (!sys.checkPermission(Feature.FORECAST)) throw new AgriException("Access denied for your role.");
        if (crop == null || crop.trim().isEmpty() || season.trim().isEmpty()) throw new AgriException("Crop and season label are required.");
        if (yield <= 0 || demand < 0) throw new AgriException("Yield must be positive and demand cannot be negative.");
        sys.yields.add(new YieldRecord(crop.trim(), season.trim(), yield, demand));
    }

    public double[] yieldsFor(String crop, boolean demand) {
        DynamicArray<Double> tmp = new DynamicArray<>();
        for (YieldRecord y : sys.yields)
            if (y.crop.equalsIgnoreCase(crop)) tmp.add(demand ? y.demand : y.yield);
        double[] out = new double[tmp.size()];
        for (int i = 0; i < tmp.size(); i++) out[i] = tmp.get(i);
        return out;
    }

    public static double movingAverage(double[] data, int window) {
        if (window <= 0 || data.length < window)
            throw new AgriException("Insufficient historical data points for a window of " + window + " seasons.");
        double sum = 0;
        for (int i = data.length - window; i < data.length; i++) sum += data[i];
        return sum / window;
    }

    public static double forecastSeedNeed(double[] yields, int window, double seedRatio) {
        return movingAverage(yields, window) * seedRatio;
    }

    // ---------------------------------------------------------------- M13
    private String farmName(String batchId) {
        Batch b = sys.batches.search(batchId);
        if (b == null) return "-";
        Farm f = sys.farmOfBatch(b);
        return f == null ? b.farmId : f.name;
    }

    private DynamicArray<ReportEntry> raw(String type) {
        DynamicArray<ReportEntry> a = new DynamicArray<>();
        switch (type) {
            case "Harvest":
                sys.batches.inOrder(b -> {
                    StockItem s = sys.stock.search(b.batchId);
                    a.add(new ReportEntry("HARVEST", b.batchId, b.crop, farmName(b.batchId), b.harvestDate, b.quantityKg, s == null ? 0 : b.quantityKg * s.unitValue));
                });
                break;
            case "Inventory":
                sys.stock.inOrder(s -> a.add(new ReportEntry("INVENTORY", s.itemId, s.crop, farmName(s.itemId), s.harvestDate, s.remaining, s.remaining * s.unitValue)));
                break;
            case "Sales":
                for (Sale s : sys.sales) if (s.status.equals("CONFIRMED")) a.add(new ReportEntry("SALE", s.itemId, s.crop, farmName(s.itemId), s.date, s.qty, s.total()));
                break;
            case "Dispatch":
                for (Delivery d : sys.deliveries) {
                    StockItem s = sys.stock.search(d.itemId);
                    a.add(new ReportEntry("DISPATCH " + d.status, d.itemId, s == null ? "-" : s.crop, farmName(d.itemId),
                            d.dispatchDate != null ? d.dispatchDate : d.deadline, d.dispatchedQty > 0 ? d.dispatchedQty : d.qty, 0));
                }
                break;
            case "Loss":
                for (Spoilage sp : sys.spoilage) {
                    if (!sp.status.equals("APPROVED")) continue;
                    StockItem s = sys.stock.search(sp.itemId);
                    a.add(new ReportEntry("LOSS", sp.itemId, s == null ? "-" : s.crop, farmName(sp.itemId), sp.date, sp.qty, sp.lossValue));
                }
                break;
            default: throw new AgriException("Unknown report type.");
        }
        return a;
    }

    public DynamicArray<ReportEntry> generateReport(String type, String crop, String farm, String batchId, LocalDate from, LocalDate to) {
        if (!sys.checkPermission(Feature.REPORTS)) throw new AgriException("Access denied for your role.");
        DynamicArray<ReportEntry> all = raw(type);
        Sorts.mergeSort(all, (x, y) -> Long.compare(x.day(), y.day()));
        int lo = from == null ? 0 : Sorts.lowerBound(all, from.toEpochDay(), ReportEntry::day);
        int hi = to == null ? all.size() : Sorts.upperBound(all, to.toEpochDay(), ReportEntry::day);
        DynamicArray<ReportEntry> out = new DynamicArray<>();
        for (int i = lo; i < hi; i++) {
            ReportEntry e = all.get(i);
            if (crop != null && !crop.equals("All") && !e.crop.equalsIgnoreCase(crop)) continue;
            if (farm != null && !farm.equals("All") && !e.farm.equalsIgnoreCase(farm)) continue;
            if (batchId != null && !batchId.isBlank() && !e.ref.equalsIgnoreCase(batchId.trim())) continue;
            out.add(e);
        }
        return out;
    }

    public String computeEstimatedRevenue() {
        double confirmed = 0, pending = 0, inventory = 0;
        for (Sale s : sys.sales) {
            if (s.status.equals("CONFIRMED")) confirmed += s.total();
            else if (s.status.equals("PENDING")) pending += s.total();
        }
        for (StockItem s : sys.traverseInventory()) inventory += Math.max(0, s.available()) * s.unitValue;
        return String.format("ESTIMATED REVENUE%n------------------------------%nConfirmed sales          : %s%nPending orders (queued): %s%nUnreserved inventory value: %s%n------------------------------%nProjected total          : %s%n",
                Fmt.peso(confirmed), Fmt.peso(pending), Fmt.peso(inventory), Fmt.peso(confirmed + pending + inventory));
    }

    private static class CropStat { String crop; double harvested, damaged; double rate() { return harvested == 0 ? 0 : damaged / harvested; } }

    private DynamicArray<CropStat> cropStats() {
        DynamicArray<CropStat> stats = new DynamicArray<>();
        for (StockItem s : sys.traverseInventory()) {
            CropStat found = null;
            for (CropStat c : stats) if (c.crop.equalsIgnoreCase(s.crop)) { found = c; break; }
            if (found == null) { found = new CropStat(); found.crop = s.crop; stats.add(found); }
            found.harvested += s.harvested; found.damaged += s.damaged;
        }
        return stats;
    }

    public String identifyHighLossCrops() {
        DynamicArray<CropStat> stats = cropStats();
        Sorts.mergeSort(stats, (a, b) -> Double.compare(b.rate(), a.rate()));
        StringBuilder sb = new StringBuilder("HIGH-LOSS CROPS (ranked by loss rate)\n--------------------------------------\n");
        int rank = 1;
        for (CropStat c : stats) sb.append(String.format("%d. %-12s harvested %,.1f kg | lost %,.1f kg | loss rate %.1f%%%n", rank++, c.crop, c.harvested, c.damaged, c.rate() * 100));
        if (stats.isEmpty()) sb.append("No stock data yet.\n");
        return sb.toString();
    }

    public String identifySlowMoving() {
        DynamicArray<StockItem> items = new DynamicArray<>();
        for (StockItem s : sys.traverseInventory()) if (s.remaining > 0) items.add(s);
        Sorts.mergeSort(items, (a, b) -> {
            double ta = a.harvested == 0 ? 0 : a.sold / a.harvested, tb = b.harvested == 0 ? 0 : b.sold / b.harvested;
            int c = Double.compare(ta, tb);
            return c != 0 ? c : a.harvestDate.compareTo(b.harvestDate);
        });
        StringBuilder sb = new StringBuilder("SLOW-MOVING STOCK (lowest sell-through first)\n---------------------------------------------\n");
        int rank = 1;
        for (StockItem s : items) {
            long age = java.time.temporal.ChronoUnit.DAYS.between(s.harvestDate, sys.today());
            sb.append(String.format("%d. %-8s %-10s sold %.0f%% | %,.1f kg left | %d days in stock%n", rank++, s.itemId, s.crop,
                    s.harvested == 0 ? 0 : 100 * s.sold / s.harvested, s.remaining, age));
        }
        if (items.isEmpty()) sb.append("No stock on hand.\n");
        return sb.toString();
    }

    public String analyzeSeasonalTrends() {
        double[] harvested = new double[12], sold = new double[12];
        sys.batches.inOrder(b -> harvested[b.harvestDate.getMonthValue() - 1] += b.quantityKg);
        for (Sale s : sys.sales) if (s.status.equals("CONFIRMED")) sold[s.date.getMonthValue() - 1] += s.qty;
        StringBuilder sb = new StringBuilder("SEASONAL TRENDS (by month)\n---------------------------\n");
        int peakH = 0, peakS = 0;
        for (int m = 0; m < 12; m++) {
            if (harvested[m] > harvested[peakH]) peakH = m;
            if (sold[m] > sold[peakS]) peakS = m;
            if (harvested[m] > 0 || sold[m] > 0)
                sb.append(String.format("%-4s harvested %,10.1f kg | sold %,10.1f kg%n", Month.of(m + 1).getDisplayName(TextStyle.SHORT, Locale.ENGLISH), harvested[m], sold[m]));
        }
        if (harvested[peakH] > 0) sb.append("Peak harvest month: ").append(Month.of(peakH + 1).getDisplayName(TextStyle.FULL, Locale.ENGLISH)).append("\n");
        if (sold[peakS] > 0) sb.append("Peak sales month: ").append(Month.of(peakS + 1).getDisplayName(TextStyle.FULL, Locale.ENGLISH)).append("\n");
        return sb.toString();
    }
}
