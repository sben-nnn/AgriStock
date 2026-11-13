package com.agristock.service;

import com.agristock.model.AgriException;
import com.agristock.model.Models.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** MODULE 15: System state persistence - sequential read/write of one CSV file. */
public class Persistence {
    public static final Path FILE = Paths.get("data", "agristock_state.csv");

    private static String e(Object o) {
        if (o == null) return "";
        return o.toString().replace("\\", "\\\\").replace(",", "\\c").replace("\n", "\\n").replace("\r", "");
    }

    private static String u(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                sb.append(n == 'c' ? ',' : n == 'n' ? '\n' : n);
            } else sb.append(c);
        }
        return sb.toString();
    }

    private static String[] split(String line) {
        String[] raw = line.split(",", -1);
        for (int i = 0; i < raw.length; i++) raw[i] = u(raw[i]);
        return raw;
    }

    private static LocalDate d(String s) { return s.isEmpty() ? null : LocalDate.parse(s); }
    private static double n(String s) { return s.isEmpty() ? 0 : Double.parseDouble(s); }

    private static <T> List<T> oldestFirst(Iterable<T> list) {
        List<T> l = new ArrayList<>();
        for (T t : list) l.add(t);
        Collections.reverse(l);
        return l;
    }

    // ------------------------------------------------------------ SAVE
    public static void save(AgriStockSystem s) {
        try {
            Files.createDirectories(FILE.getParent());
            try (BufferedWriter w = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                line(w, "COUNTER", s.saleCounter, s.deliveryCounter, s.spoilageCounter);
                line(w, "SETTING", s.lowStockKg, s.agingDays, s.nearExpiryDays);
                s.users.inOrder(x -> line(w, "USER", x.username, x.passwordHash, x.fullName, x.role.name(), x.active));
                for (Farm x : oldestFirst(s.farms)) line(w, "FARM", x.id, x.name, x.location);
                for (Crop x : oldestFirst(s.crops)) line(w, "CROP", x.name, x.variety, x.category);
                for (Farmer f : oldestFirst(s.farmers)) {
                    line(w, "FARMER", f.id, f.name, f.contact);
                    for (HarvestLog h : oldestFirst(f.harvests)) line(w, "HLOG", f.id, h.cropType, h.quantity, h.date);
                }
                s.batches.inOrder(b -> line(w, "BATCH", b.batchId, b.farmId, b.plot, b.crop, b.variety, b.worker, b.farmerId,
                        b.unit, b.originalQty, b.quantityKg, b.harvestDate, b.expiryDate, b.status.name(), b.grade));
                for (StockRecord r : oldestFirst(s.stockRecords))
                    line(w, "SREC", r.batchId, r.harvestedQty, r.rejectedQty, r.saleableQty, r.grade, r.ripeness, r.rejectReason, r.moisture, r.recorded);
                for (Batch b : s.inspectionQueue) line(w, "INSPQ", b.batchId);
                for (StorageEntry x : s.rotationQueue) line(w, "STORE", x.batchId, x.location, x.qty, x.storageDate, x.conditions, x.rotations);
                s.stock.inOrder(x -> line(w, "STOCK", x.itemId, x.crop, x.category, x.harvestDate, x.expiryDate, x.grade, x.location,
                        x.harvested, x.stored, x.reserved, x.sold, x.dispatched, x.damaged, x.remaining, x.unitValue));
                for (Sale x : oldestFirst(s.sales)) line(w, "SALE", x.ref, x.buyer, x.itemId, x.crop, x.qty, x.unitPrice, x.date, x.status, x.deliveryScheduled);
                for (Delivery x : oldestFirst(s.deliveries)) line(w, "DELIV", x.id, x.saleRef, x.company, x.itemId, x.qty, x.dispatchedQty,
                        x.receivedQty, x.deadline, x.dispatchDate, x.receivedDate, x.urgency, x.vehicle, x.destination, x.status);
                for (Spoilage x : oldestFirst(s.spoilage)) line(w, "SPOIL", x.id, x.itemId, x.qty, x.reason, x.reporter, x.approver, x.lossValue, x.date, x.status, x.kind);
                for (YieldRecord y : s.yields) line(w, "YIELD", y.crop, y.season, y.yield, y.demand);
                for (AuditEntry a : oldestFirst(s.audit)) line(w, "AUDIT", a.type, a.ref, a.user, a.note, a.recorded, a.physical, a.discrepancy, a.time);
            }
        } catch (IOException | UncheckedIOException ex) {
            throw new AgriException("Could not save system state: " + ex.getMessage());
        }
    }

    private static void line(Writer w, String type, Object... f) {
        try {
            StringBuilder sb = new StringBuilder(type);
            for (Object o : f) sb.append(',').append(e(o));
            w.write(sb.append('\n').toString());
        } catch (IOException ex) { throw new UncheckedIOException(ex); }
    }

    // ------------------------------------------------------------ LOAD
    /** @return true if a saved state was found and loaded. */
    public static boolean load(AgriStockSystem s) {
        if (!Files.exists(FILE)) return false;
        try (BufferedReader r = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            String ln;
            while ((ln = r.readLine()) != null) {
                if (ln.isBlank()) continue;
                String[] t = split(ln);
                switch (t[0]) {
                    case "COUNTER": s.saleCounter = (int) n(t[1]); s.deliveryCounter = (int) n(t[2]); s.spoilageCounter = (int) n(t[3]); break;
                    case "SETTING": s.lowStockKg = n(t[1]); s.agingDays = (int) n(t[2]); s.nearExpiryDays = (int) n(t[3]); break;
                    case "USER": { User x = new User(t[1], t[2], t[3], Role.valueOf(t[4])); x.active = Boolean.parseBoolean(t[5]); s.users.insert(x.username, x); break; }
                    case "FARM": s.farms.addFirst(new Farm(t[1], t[2], t[3])); break;
                    case "CROP": s.crops.addFirst(new Crop(t[1], t[2], t[3])); break;
                    case "FARMER": s.farmers.addFirst(new Farmer(t[1], t[2], t[3])); break;
                    case "HLOG": { Farmer f = s.findFarmer(t[1]); if (f != null) f.harvests.addFirst(new HarvestLog(t[2], n(t[3]), d(t[4]))); break; }
                    case "BATCH": {
                        Batch b = new Batch();
                        b.batchId = t[1]; b.farmId = t[2]; b.plot = t[3]; b.crop = t[4]; b.variety = t[5]; b.worker = t[6]; b.farmerId = t[7];
                        b.unit = t[8]; b.originalQty = n(t[9]); b.quantityKg = n(t[10]); b.harvestDate = d(t[11]); b.expiryDate = d(t[12]);
                        b.status = BatchStatus.valueOf(t[13]); b.grade = t[14];
                        s.batches.insert(b.batchId, b); break;
                    }
                    case "SREC": {
                        StockRecord x = new StockRecord();
                        x.batchId = t[1]; x.harvestedQty = n(t[2]); x.rejectedQty = n(t[3]); x.saleableQty = n(t[4]); x.grade = t[5];
                        x.ripeness = t[6]; x.rejectReason = t[7]; x.moisture = n(t[8]); x.recorded = d(t[9]);
                        s.stockRecords.addFirst(x); break;
                    }
                    case "INSPQ": { Batch b = s.batches.search(t[1]); if (b != null) s.inspectionQueue.enqueue(b); break; }
                    case "STORE": {
                        StorageEntry x = new StorageEntry();
                        x.batchId = t[1]; x.location = t[2]; x.qty = n(t[3]); x.storageDate = d(t[4]); x.conditions = t[5]; x.rotations = (int) n(t[6]);
                        s.rotationQueue.enqueue(x); break;
                    }
                    case "STOCK": {
                        StockItem x = new StockItem();
                        x.itemId = t[1]; x.crop = t[2]; x.category = t[3]; x.harvestDate = d(t[4]); x.expiryDate = d(t[5]); x.grade = t[6]; x.location = t[7];
                        x.harvested = n(t[8]); x.stored = n(t[9]); x.reserved = n(t[10]); x.sold = n(t[11]); x.dispatched = n(t[12]);
                        x.damaged = n(t[13]); x.remaining = n(t[14]); x.unitValue = n(t[15]);
                        s.stock.insert(x.itemId, x); break;
                    }
                    case "SALE": {
                        Sale x = new Sale();
                        x.ref = t[1]; x.buyer = t[2]; x.itemId = t[3]; x.crop = t[4]; x.qty = n(t[5]); x.unitPrice = n(t[6]); x.date = d(t[7]);
                        x.status = t[8]; x.deliveryScheduled = Boolean.parseBoolean(t[9]);
                        s.sales.addFirst(x);
                        if (x.status.equals("PENDING")) s.pendingSales.enqueue(x);
                        break;
                    }
                    case "DELIV": {
                        Delivery x = new Delivery();
                        x.id = t[1]; x.saleRef = t[2]; x.company = t[3]; x.itemId = t[4]; x.qty = n(t[5]); x.dispatchedQty = n(t[6]); x.receivedQty = n(t[7]);
                        x.deadline = d(t[8]); x.dispatchDate = d(t[9]); x.receivedDate = d(t[10]); x.urgency = (int) n(t[11]);
                        x.vehicle = t[12]; x.destination = t[13]; x.status = t[14];
                        s.deliveries.addFirst(x);
                        if (x.status.equals("SCHEDULED")) s.deliveryQueue.enqueue(x);
                        break;
                    }
                    case "SPOIL": {
                        Spoilage x = new Spoilage();
                        x.id = t[1]; x.itemId = t[2]; x.qty = n(t[3]); x.reason = t[4]; x.reporter = t[5]; x.approver = t[6];
                        x.lossValue = n(t[7]); x.date = d(t[8]); x.status = t[9]; x.kind = t[10];
                        s.spoilage.addFirst(x); break;
                    }
                    case "YIELD": s.yields.add(new YieldRecord(t[1], t[2], n(t[3]), n(t[4]))); break;
                    case "AUDIT": {
                        AuditEntry a = new AuditEntry();
                        a.type = t[1]; a.ref = t[2]; a.user = t[3]; a.note = t[4]; a.recorded = n(t[5]); a.physical = n(t[6]);
                        a.discrepancy = n(t[7]); a.time = LocalDateTime.parse(t[8]);
                        s.audit.addFirst(a); break;
                    }
                    default: break;
                }
            }
            s.rebuildExpiryHeap();
            return true;
        } catch (IOException | RuntimeException ex) {
            throw new AgriException("Saved data could not be read (" + ex.getMessage() + "). Rename or delete data/agristock_state.csv to start fresh.");
        }
    }
}
