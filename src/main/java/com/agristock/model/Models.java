package com.agristock.model;

import com.agristock.ds.SinglyLinkedList;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class Models {
    private Models() {}

    public enum Role {
        OWNER("Farm Owner"), MANAGER("Farm Manager"), INVENTORY("Inventory Staff"), SALES("Sales / Dispatch");
        public final String label;
        Role(String l) { label = l; }
        @Override public String toString() { return label; }
    }

    public enum Feature {
        USERS(Role.OWNER),
        FARMERS(Role.OWNER, Role.MANAGER),
        BATCHES(Role.OWNER, Role.MANAGER, Role.INVENTORY),
        HARVEST_STOCK(Role.OWNER, Role.MANAGER, Role.INVENTORY),
        INSPECTION(Role.OWNER, Role.MANAGER, Role.INVENTORY),
        STORAGE(Role.OWNER, Role.MANAGER, Role.INVENTORY),
        STOCK_EDIT(Role.OWNER, Role.MANAGER, Role.INVENTORY),
        EXPIRY(Role.OWNER, Role.MANAGER, Role.INVENTORY, Role.SALES),
        SALES(Role.OWNER, Role.MANAGER, Role.SALES),
        DISPATCH(Role.OWNER, Role.MANAGER, Role.SALES),
        SPOILAGE_LOG(Role.OWNER, Role.MANAGER, Role.INVENTORY, Role.SALES),
        SPOILAGE_APPROVE(Role.OWNER, Role.MANAGER),
        FORECAST(Role.OWNER, Role.MANAGER),
        REPORTS(Role.OWNER, Role.MANAGER),
        AUDIT_VIEW(Role.OWNER, Role.MANAGER, Role.INVENTORY),
        AUDIT_RECONCILE(Role.OWNER, Role.MANAGER),
        PERSISTENCE(Role.OWNER, Role.MANAGER);

        private final Role[] allowed;
        Feature(Role... r) { allowed = r; }
        public boolean allows(Role role) {
            for (Role r : allowed) if (r == role) return true;
            return false;
        }
    }

    public enum BatchStatus { REGISTERED, AWAITING_INSPECTION, ACCEPTED, DOWNGRADED, REJECTED }

    public static class User {
        public String username, passwordHash, fullName;
        public Role role;
        public boolean active = true;
        public User(String u, String hash, String name, Role r) { username = u; passwordHash = hash; fullName = name; role = r; }
    }

    public static class Farm {
        public String id, name, location;
        public Farm(String i, String n, String l) { id = i; name = n; location = l; }
    }

    public static class Crop {
        public String name, variety, category;
        public Crop(String n, String v, String c) { name = n; variety = v; category = c; }
        @Override public String toString() { return name + " - " + variety; }
    }

    public static class HarvestLog {
        public String cropType; public double quantity; public LocalDate date;
        public HarvestLog(String c, double q, LocalDate d) { cropType = c; quantity = q; date = d; }
    }

    public static class Farmer {
        public String id, name, contact;
        public SinglyLinkedList<HarvestLog> harvests = new SinglyLinkedList<>();
        public Farmer(String i, String n, String c) { id = i; name = n; contact = c; }
        public double totalHarvest() { double t = 0; for (HarvestLog h : harvests) t += h.quantity; return t; }
    }

    public static class Batch {
        public String batchId, farmId, plot, crop, variety, worker, farmerId, unit, grade = "";
        public double originalQty, quantityKg;
        public LocalDate harvestDate, expiryDate;
        public BatchStatus status = BatchStatus.REGISTERED;
    }

    public static class StockRecord {
        public String batchId, grade, ripeness, rejectReason;
        public double harvestedQty, rejectedQty, saleableQty, moisture;
        public LocalDate recorded;
    }

    public static class StorageEntry {
        public String batchId, location, conditions;
        public double qty; public LocalDate storageDate; public int rotations;
    }

    public static class StockItem {
        public String itemId, crop, category, grade = "", location = "Unassigned";
        public LocalDate harvestDate, expiryDate;
        public double harvested, stored, reserved, sold, dispatched, damaged, remaining, unitValue;
        public double available() { return remaining - reserved; }
    }

    public static class Sale {
        public String ref, buyer, itemId, crop, status = "PENDING";
        public double qty, unitPrice; public LocalDate date; public boolean deliveryScheduled;
        public double total() { return qty * unitPrice; }
    }

    public static class Delivery {
        public String id, saleRef, company, itemId, vehicle, destination, status = "SCHEDULED";
        public double qty, dispatchedQty, receivedQty;
        public LocalDate deadline, dispatchDate, receivedDate;
        public int urgency = 2; // 1 = high, 2 = normal, 3 = low
    }

    public static class Spoilage {
        public String id, itemId, reason, reporter, approver = "", status = "PENDING", kind = "Spoilage";
        public double qty, lossValue; public LocalDate date;
    }

    public static class YieldRecord {
        public String crop, season; public double yield, demand;
        public YieldRecord(String c, String s, double y, double d) { crop = c; season = s; yield = y; demand = d; }
    }

    public static class AuditEntry {
        public String type, ref, user, note;
        public double recorded, physical, discrepancy;
        public LocalDateTime time;
    }

    public static class ReportEntry {
        public String type, ref, crop, farm;
        public LocalDate date; public double qty, value;
        public ReportEntry(String t, String r, String c, String f, LocalDate d, double q, double v) {
            type = t; ref = r; crop = c; farm = f; date = d; qty = q; value = v;
        }
        public long day() { return date.toEpochDay(); }
    }
}
