package com.agristock.service;

import com.agristock.ds.*;
import com.agristock.model.AgriException;
import com.agristock.model.Models.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AgriStockSystem {

    // Data structures
    public final AVLTree<String, User> users = new AVLTree<>();                       // M1
    public final SinglyLinkedList<AuditEntry> audit = new SinglyLinkedList<>();       // M1 log + M14 audit trail
    public final SinglyLinkedList<Farmer> farmers = new SinglyLinkedList<>();         // M2
    public final SinglyLinkedList<Farm> farms = new SinglyLinkedList<>();             // M3
    public final SinglyLinkedList<Crop> crops = new SinglyLinkedList<>();             // M3
    public final AVLTree<String, Batch> batches = new AVLTree<>();                    // M3
    public final SinglyLinkedList<StockRecord> stockRecords = new SinglyLinkedList<>(); // M4
    public final LinkedQueue<Batch> inspectionQueue = new LinkedQueue<>();            // M5
    public final String[] locations = {"Cold Room A", "Cold Room B", "Dry Warehouse 1",
            "Dry Warehouse 2", "Grain Silo 1", "Grain Silo 2", "Seed Vault", "Open-Air Shed"}; // M6 array
    public final LinkedQueue<StorageEntry> rotationQueue = new LinkedQueue<>();       // M6
    public final AVLTree<String, StockItem> stock = new AVLTree<>();                  // M7
    public final MinHeap<StockItem> expiryHeap = new MinHeap<>(                       // M8
            (a, b) -> { int c = a.expiryDate.compareTo(b.expiryDate); return c != 0 ? c : a.itemId.compareTo(b.itemId); });
    public final LinkedQueue<Sale> pendingSales = new LinkedQueue<>();                // M9
    public final SinglyLinkedList<Sale> sales = new SinglyLinkedList<>();             // M9 records
    public final DeliveryPriorityQueue<Delivery> deliveryQueue = new DeliveryPriorityQueue<>( // M10
            (a, b) -> { int c = a.deadline.compareTo(b.deadline); return c != 0 ? c : Integer.compare(a.urgency, b.urgency); });
    public final SinglyLinkedList<Delivery> deliveries = new SinglyLinkedList<>();    // M10 records
    public final SinglyLinkedList<Spoilage> spoilage = new SinglyLinkedList<>();      // M11
    public final DynamicArray<YieldRecord> yields = new DynamicArray<>();             // M12

    public User session;
    public double lowStockKg = 50;
    public int agingDays = 14, nearExpiryDays = 7;
    public int saleCounter, deliveryCounter, spoilageCounter;

    public LocalDate today() { return LocalDate.now(); }

    // MODULE 1: User & Role Management (AVL Tree keyed by username)
    public static String hash(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    public void seedDefaultUsers() {
        users.insert("admin", new User("admin", hash("admin123"), "Farm Owner (default)", Role.OWNER));
        users.insert("manager", new User("manager", hash("manager123"), "Farm Manager (default)", Role.MANAGER));
        users.insert("inventory", new User("inventory", hash("inventory123"), "Inventory Staff (default)", Role.INVENTORY));
        users.insert("sales", new User("sales", hash("sales123"), "Sales / Dispatch (default)", Role.SALES));
    }
    
    public User login(String username, String password) {
        User u = users.search(username.trim());
        if (u == null) throw new AgriException("User not found.");
        if (!u.active || !u.passwordHash.equals(hash(password)))
            throw new AgriException("Invalid credentials or deactivated account.");
        session = u;
        logTransaction("LOGIN", u.username, "Authenticated session granted");
        return u;
    }

    public void logout() {
        if (session != null) logTransaction("LOGOUT", session.username, "Session ended");
        session = null;
    }

    public boolean checkPermission(Feature f) { return session != null && f.allows(session.role); }

    private void require(Feature f) {
        if (!checkPermission(f))
            throw new AgriException("Access denied: the role '" + (session == null ? "none" : session.role.label)
                    + "' is not permitted to perform this action.");
    }

    public void addUser(String username, String fullName, String password, Role role) {
        require(Feature.USERS);
        username = username.trim();
        if (username.isEmpty() || fullName.trim().isEmpty() || password.isEmpty() || role == null)
            throw new AgriException("Username, full name, password and role are required.");
        if (!users.insert(username, new User(username, hash(password), fullName.trim(), role)))
            throw new AgriException("Username '" + username + "' already exists.");
        logTransaction("USER_ADD", username, "Created user with role " + role.label);
    }

    public void editUser(String username, String fullName, String newPassword) {
        require(Feature.USERS);
        User u = mustFindUser(username);
        if (!fullName.trim().isEmpty()) u.fullName = fullName.trim();
        if (!newPassword.isEmpty()) u.passwordHash = hash(newPassword);
        logTransaction("USER_EDIT", username, "User details updated");
    }

    public void assignRole(String username, Role role) {
        require(Feature.USERS);
        User u = mustFindUser(username);
        if (u == session) throw new AgriException("You cannot change your own role.");
        u.role = role;
        logTransaction("ROLE_ASSIGN", username, "Role set to " + role.label);
    }

    public void setUserActive(String username, boolean active) {
        require(Feature.USERS);
        User u = mustFindUser(username);
        if (u == session) throw new AgriException("You cannot deactivate your own account.");
        u.active = active;
        logTransaction(active ? "USER_ACTIVATE" : "USER_DEACTIVATE", username, active ? "Account reactivated" : "Account deactivated");
    }

    private User mustFindUser(String username) {
        User u = users.search(username);
        if (u == null) throw new AgriException("Select an existing user first.");
        return u;
    }

    public void logTransaction(String type, String ref, String note) { logAuditTrail(type, ref, 0, 0, note); }

    // MODULE 2: Farmer Tracking (Linked List + Linear Search)
    public Farmer findFarmer(String id) { return farmers.linearSearch(f -> f.id.equalsIgnoreCase(id)); }

    public void registerFarmer(String id, String name, String contact) {
        require(Feature.FARMERS);
        id = id.trim();
        if (id.isEmpty() || name.trim().isEmpty()) throw new AgriException("Farmer ID and name are required.");
        if (findFarmer(id) != null) throw new AgriException("Farmer already exists in registry.");
        farmers.addFirst(new Farmer(id, name.trim(), contact.trim()));
        logTransaction("FARMER_ADD", id, "Registered farmer " + name);
    }

    public void recordHarvest(String farmerId, String crop, double qty, LocalDate date) {
        require(Feature.FARMERS);
        Farmer f = findFarmer(farmerId);
        if (f == null) throw new AgriException("Farmer ID not found.");
        if (crop == null || crop.trim().isEmpty()) throw new AgriException("Crop type is required.");
        if (qty <= 0) throw new AgriException("Quantity must be greater than zero.");
        if (date == null) throw new AgriException("Harvest date is required.");
        f.harvests.addFirst(new HarvestLog(crop.trim(), qty, date));
        logTransaction("FARMER_HARVEST", farmerId, qty + " kg of " + crop);
    }

    // MODULE 3: Farm, Crop & Harvest Batch Registration (AVL Tree by batch ID)
    public static final String[] UNITS = {"kg", "g", "lb", "ton", "sack (50 kg)"};

    public static double convertUnit(double qty, String unit) {
        switch (unit) {
            case "g": return qty / 1000.0;
            case "lb": return qty * 0.45359237;
            case "ton": return qty * 1000.0;
            case "sack (50 kg)": return qty * 50.0;
            default: return qty;
        }
    }

    public Farm findFarm(String id) { return farms.linearSearch(f -> f.id.equalsIgnoreCase(id)); }

    public void registerFarm(String id, String name, String location) {
        require(Feature.BATCHES);
        if (id.trim().isEmpty() || name.trim().isEmpty()) throw new AgriException("Farm/field ID and name are required.");
        if (findFarm(id.trim()) != null) throw new AgriException("Farm ID already exists.");
        farms.addFirst(new Farm(id.trim(), name.trim(), location.trim()));
        logTransaction("FARM_ADD", id.trim(), "Registered farm " + name);
    }

    public void registerCrop(String name, String variety, String category) {
        require(Feature.BATCHES);
        if (name.trim().isEmpty() || variety.trim().isEmpty() || category == null) throw new AgriException("Crop name, variety and category are required.");
        if (crops.linearSearch(c -> c.name.equalsIgnoreCase(name.trim()) && c.variety.equalsIgnoreCase(variety.trim())) != null)
            throw new AgriException("That crop variety is already registered.");
        crops.addFirst(new Crop(name.trim(), variety.trim(), category));
        logTransaction("CROP_ADD", name.trim(), "Registered crop variety " + variety);
    }

    public String categoryOf(String cropName) {
        Crop c = crops.linearSearch(x -> x.name.equalsIgnoreCase(cropName));
        return c == null ? "Other" : c.category;
    }

    // Returns an error message, or null if the batch ID is valid and unused (AVL search).
    public String validateBatchID(String id) {
        if (id == null || id.trim().isEmpty()) return "Batch ID is required.";
        if (!id.trim().matches("[A-Za-z0-9\\-]+")) return "Batch ID may only contain letters, digits and hyphens.";
        if (batches.search(id.trim()) != null) return "Duplicate batch ID detected. Operation aborted.";
        return null;
    }

    public String nextBatchId() {
        int n = batches.size() + 1001;
        while (batches.search("B-" + n) != null) n++;
        return "B-" + n;
    }

    public Batch createHarvestBatch(String batchId, String farmId, String plot, String crop, String variety,
                                    String worker, String farmerId, LocalDate harvest, LocalDate expiry,
                                    double qty, String unit) {
        require(Feature.BATCHES);
        String err = validateBatchID(batchId);
        if (err != null) throw new AgriException(err);
        if (findFarm(farmId == null ? "" : farmId) == null) throw new AgriException("Select a registered farm.");
        if (crop == null) throw new AgriException("Select a registered crop.");
        if (farmerId != null && !farmerId.isEmpty() && findFarmer(farmerId) == null) throw new AgriException("Farmer not found.");
        if (harvest == null || expiry == null) throw new AgriException("Harvest date and expiry date are required.");
        if (!expiry.isAfter(harvest)) throw new AgriException("Expiry date must be after the harvest date.");
        if (qty <= 0) throw new AgriException("Quantity must be greater than zero.");
        Batch b = new Batch();
        b.batchId = batchId.trim(); b.farmId = farmId; b.plot = plot.trim(); b.crop = crop; b.variety = variety;
        b.worker = worker.trim(); b.farmerId = farmerId == null ? "" : farmerId; b.unit = unit; b.originalQty = qty;
        b.quantityKg = convertUnit(qty, unit); b.harvestDate = harvest; b.expiryDate = expiry;
        batches.insert(b.batchId, b);                                  // AVL insert + rebalancing
        logAuditTrail("HARVEST", b.batchId, b.quantityKg, 0, "Batch registered: " + crop + " (" + variety + ")");
        return b;
    }

    public Farm farmOfBatch(Batch b) { return findFarm(b.farmId); }

    // MODULE 4: Harvest Stock Recording (Linked List + Linear Search)
    public StockRecord findStockRecord(String batchId) { return stockRecords.linearSearch(r -> r.batchId.equals(batchId)); }

    public void validateQuantity(double harvested, double rejected, double moisture) {
        if (harvested <= 0) throw new AgriException("Harvested quantity must be greater than zero.");
        if (rejected < 0) throw new AgriException("Invalid or negative rejected quantity entered.");
        if (rejected > harvested) throw new AgriException("Rejected quantity cannot exceed harvested quantity.");
        if (moisture < 0 || moisture > 100) throw new AgriException("Moisture must be between 0 and 100 %.");
    }

    public StockRecord recordHarvestedStock(String batchId, double harvested, double rejected, String grade,
                                            String ripeness, double moisture, String rejectReason) {
        require(Feature.HARVEST_STOCK);
        Batch b = batches.search(batchId == null ? "" : batchId);
        if (b == null) throw new AgriException("Select a registered batch.");
        if (findStockRecord(batchId) != null) throw new AgriException("Batch stock entry already logged.");   // linear search
        validateQuantity(harvested, rejected, moisture);
        if (harvested > b.quantityKg + 0.0001) throw new AgriException("Harvested quantity cannot exceed the registered batch quantity ("
                + String.format("%.2f", b.quantityKg) + " kg).");
        if (rejected > 0 && (rejectReason == null || rejectReason.trim().isEmpty()))
            throw new AgriException("Enter a reason for the rejected/damaged produce.");
        StockRecord r = new StockRecord();
        r.batchId = batchId; r.harvestedQty = harvested; r.grade = grade; r.ripeness = ripeness; r.moisture = moisture;
        r.recorded = today(); r.rejectReason = rejectReason == null ? "" : rejectReason.trim();
        r.rejectedQty = 0; r.saleableQty = harvested;
        stockRecords.addFirst(r);
        if (rejected > 0) recordRejectedStock(batchId, rejected, r.rejectReason);
        b.grade = grade;
        logAuditTrail("HARVEST", batchId, harvested, 0, "Harvest stock recorded; rejected " + rejected + " kg");
        enqueueForInspection(b);
        return r;
    }

    // Moves produce from saleable to rejected on a stock record (only before inspection).
    public void recordRejectedStock(String batchId, double qty, String reason) {
        StockRecord r = findStockRecord(batchId);
        if (r == null) throw new AgriException("No stock record for batch " + batchId);
        if (qty <= 0 || qty > r.saleableQty) throw new AgriException("Invalid rejected quantity.");
        r.rejectedQty += qty; r.saleableQty -= qty; r.rejectReason = reason;
    }

    // MODULE 5: Quality Inspection & Grading (Queue, FIFO scheduling)
    public void enqueueForInspection(Batch b) {
        b.status = BatchStatus.AWAITING_INSPECTION;
        inspectionQueue.enqueue(b);
    }

    public Batch inspectBatch(String status, String grade, String notes, double unitValue) {
        require(Feature.INSPECTION);
        if (inspectionQueue.isEmpty()) throw new AgriException("No batches awaiting inspection.");
        if (status == null) throw new AgriException("Select an inspection result.");
        boolean eligible = status.equals("ACCEPTED") || status.equals("DOWNGRADED");
        if (!status.equals("ACCEPTED") && notes.trim().isEmpty()) throw new AgriException("A justification is required for downgraded or rejected batches.");
        if (eligible && unitValue <= 0) throw new AgriException("Enter an estimated unit value (per kg) greater than zero.");
        Batch b = inspectionQueue.dequeue();                           // FIFO: front element
        gradeBatch(b, grade);
        b.status = BatchStatus.valueOf(status);
        StockRecord r = findStockRecord(b.batchId);
        r.grade = grade;
        if (eligible) moveToSaleableInventory(b, r, unitValue);
        else { r.rejectedQty += r.saleableQty; r.saleableQty = 0; r.rejectReason = notes.trim(); }
        logAuditTrail("INSPECTION", b.batchId, r.saleableQty, 0, status + " grade " + grade + (notes.isBlank() ? "" : " - " + notes.trim()));
        return b;
    }

    public void gradeBatch(Batch b, String grade) { b.grade = grade; }

    private void moveToSaleableInventory(Batch b, StockRecord r, double unitValue) {
        StockItem s = new StockItem();
        s.itemId = b.batchId; s.crop = b.crop; s.category = categoryOf(b.crop);
        s.harvestDate = b.harvestDate; s.expiryDate = b.expiryDate; s.grade = b.grade;
        s.harvested = r.harvestedQty; s.damaged = r.rejectedQty; s.remaining = r.saleableQty; s.unitValue = unitValue;
        stock.insert(s.itemId, s);
        if (s.remaining > 0) expiryHeap.insert(s);
    }

    // MODULE 6: Storage & Location Management (Array + FIFO Queue rotation)
    public StorageEntry assignLocation(String itemId, int locationIndex, double qty, LocalDate date, String conditions) {
        require(Feature.STORAGE);
        if (locationIndex < 0 || locationIndex >= locations.length) throw new AgriException("Invalid storage location.");
        StockItem s = stock.search(itemId == null ? "" : itemId);
        if (s == null) throw new AgriException("Select a batch that has passed inspection.");
        if (qty <= 0) throw new AgriException("Quantity must be greater than zero.");
        if (qty > Math.max(0, s.remaining - s.stored) + 0.0001) throw new AgriException("Only "
                + String.format("%.2f", Math.max(0, s.remaining - s.stored)) + " kg of this batch is not yet stored.");
        if (date == null) throw new AgriException("Storage date is required.");
        StorageEntry e = new StorageEntry();
        e.batchId = itemId; e.location = locations[locationIndex]; e.qty = qty; e.storageDate = date;
        e.conditions = conditions == null ? "" : conditions.trim();
        rotationQueue.enqueue(e);                                      // FIFO rotation order
        s.stored += qty;
        s.location = s.location.equals("Unassigned") || s.location.equals(e.location) ? e.location : "Multiple";
        logAuditTrail("TRANSFER", itemId, qty, 0, "Stored at " + e.location);
        return e;
    }

    public void recordStorageConditions(StorageEntry e, String conditions) {
        require(Feature.STORAGE);
        e.conditions = conditions.trim();
        logAuditTrail("TRANSFER", e.batchId, e.qty, 0, "Storage conditions updated: " + e.conditions);
    }

    // FIFO rotation: the oldest stored lot is moved to the front for use, then re-queued at the back.
    public StorageEntry rotateStockFIFO() {
        require(Feature.STORAGE);
        if (rotationQueue.isEmpty()) throw new AgriException("No stored batches to rotate.");
        StorageEntry oldest = rotationQueue.dequeue();
        oldest.rotations++;
        rotationQueue.enqueue(oldest);
        logAuditTrail("TRANSFER", oldest.batchId, oldest.qty, 0, "FIFO rotation - oldest lot at " + oldest.location + " used first");
        return oldest;
    }

    // MODULE 7: Stock Monitoring & Control (AVL Tree, in-order traversal, binary search)
    public StockItem addItem(String itemId, String crop, String category, double qty, LocalDate expiry, double unitValue) {
        require(Feature.STOCK_EDIT);
        if (itemId.trim().isEmpty() || crop.trim().isEmpty() || category == null) throw new AgriException("Item ID, crop and category are required.");
        if (qty <= 0) throw new AgriException("Quantity must be greater than zero.");
        if (expiry == null || !expiry.isAfter(today().minusDays(1))) throw new AgriException("Enter an expiry date that is not in the past.");
        StockItem s = new StockItem();
        s.itemId = itemId.trim(); s.crop = crop.trim(); s.category = category; s.harvestDate = today(); s.expiryDate = expiry;
        s.harvested = qty; s.remaining = qty; s.unitValue = unitValue;
        if (!stock.insert(s.itemId, s)) throw new AgriException("Item ID already exists in stock.");
        expiryHeap.insert(s);
        logAuditTrail("ADJUSTMENT", s.itemId, qty, 0, "Manual stock item added");
        return s;
    }

    public void editItem(String itemId, String category, LocalDate expiry, double unitValue) {
        require(Feature.STOCK_EDIT);
        StockItem s = mustFindStock(itemId);
        if (expiry == null) throw new AgriException("Expiry date is required.");
        s.category = category; s.expiryDate = expiry; s.unitValue = unitValue;
        rebuildExpiryHeap();
        logAuditTrail("ADJUSTMENT", itemId, s.remaining, 0, "Item details edited");
    }

    public DynamicArray<StockItem> traverseInventory() { return stock.toSortedArray(); }   // in-order traversal

    public StockItem searchStock(String itemId) { return stock.search(itemId.trim()); }    // binary search

    public StockItem mustFindStock(String itemId) {
        StockItem s = itemId == null ? null : stock.search(itemId);
        if (s == null) throw new AgriException("Stock item not found.");
        return s;
    }

    public DynamicArray<StockItem> filterStock(String text, String category, String flag) {
        DynamicArray<StockItem> out = new DynamicArray<>();
        String t = text == null ? "" : text.trim().toLowerCase();
        for (StockItem s : traverseInventory()) {
            if (!t.isEmpty() && !s.itemId.toLowerCase().contains(t) && !s.crop.toLowerCase().contains(t)) continue;
            if (category != null && !category.equals("All") && !s.category.equals(category)) continue;
            if (flag != null && !flag.equals("All") && !flagsOf(s).contains(flag)) continue;
            out.add(s);
        }
        return out;
    }

    public double computeAvailableStock(StockItem s) { return s.available(); }
    public boolean flagLowStock(StockItem s) { return s.remaining > 0 && s.available() <= lowStockKg; }
    public boolean flagAgingStock(StockItem s) { return s.remaining > 0 && s.harvestDate.plusDays(agingDays).isBefore(today()); }
    public boolean flagExpired(StockItem s) { return s.remaining > 0 && s.expiryDate.isBefore(today()); }
    public boolean flagNearExpiry(StockItem s) {
        return s.remaining > 0 && !flagExpired(s) && !s.expiryDate.isAfter(today().plusDays(nearExpiryDays));
    }

    public String flagsOf(StockItem s) {
        StringBuilder sb = new StringBuilder();
        if (flagExpired(s)) sb.append("Expired ");
        if (flagNearExpiry(s)) sb.append("Near-expiry ");
        if (flagLowStock(s)) sb.append("Low ");
        if (flagAgingStock(s)) sb.append("Aging ");
        return sb.toString().trim();
    }

    public void reserveStock(String itemId, double qty) {
        StockItem s = mustFindStock(itemId);
        if (qty <= 0) throw new AgriException("Quantity must be greater than zero.");
        if (qty > s.available() + 0.0001) throw new AgriException("Only " + String.format("%.2f", s.available()) + " kg available to reserve.");
        s.reserved += qty;
    }

    public void releaseReservation(String itemId, double qty) {
        StockItem s = mustFindStock(itemId);
        if (qty <= 0 || qty > s.reserved + 0.0001) throw new AgriException("Cannot release more than the reserved quantity (" + String.format("%.2f", s.reserved) + " kg).");
        s.reserved = Math.max(0, s.reserved - qty);
    }

    public void reserveManually(String itemId, double qty) {
        require(Feature.STOCK_EDIT);
        reserveStock(itemId, qty);
        logAuditTrail("ADJUSTMENT", itemId, qty, 0, "Stock reserved");
    }

    public void releaseManually(String itemId, double qty) {
        require(Feature.STOCK_EDIT);
        releaseReservation(itemId, qty);
        logAuditTrail("ADJUSTMENT", itemId, qty, 0, "Reservation released");
    }

    // MODULE 8: Expiry & Purge (Min-Heap, FEFO / extract-min)
    public void rebuildExpiryHeap() {
        expiryHeap.clear();
        stock.inOrder(s -> { if (s.remaining > 0) expiryHeap.insert(s); });
    }

    public List<StockItem> checkAndPurgeExpired() {
        require(Feature.EXPIRY);
        List<StockItem> purged = new ArrayList<>();
        while (!expiryHeap.isEmpty()) {
            StockItem root = expiryHeap.peek();
            if (root.remaining <= 0) { expiryHeap.extractMin(); continue; }
            if (!root.expiryDate.isBefore(today())) break;             // earliest is still fresh -> all fresh
            expiryHeap.extractMin();
            double lost = root.remaining;
            cancelPendingSalesFor(root.itemId);
            root.damaged += lost; root.remaining = 0; root.reserved = 0;
            Spoilage sp = newSpoilage(root.itemId, lost, "Expired - purged by FEFO check", "SYSTEM");
            sp.kind = "Expired purge"; sp.status = "APPROVED"; sp.approver = session.username; sp.lossValue = lost * root.unitValue;
            logAuditTrail("ADJUSTMENT", root.itemId, lost, 0, "Expired stock purged");
            purged.add(root);
        }
        return purged;
    }

    private void cancelPendingSalesFor(String itemId) {
        List<Sale> toCancel = new ArrayList<>();
        for (Sale s : pendingSales) if (s.itemId.equals(itemId)) toCancel.add(s);
        for (Sale s : toCancel) { pendingSales.remove(s); s.status = "CANCELLED"; }
    }

    // FEFO dispatch: always takes stock from the earliest-expiring batch first.
    public List<String> dispatchFEFO(String cropFilter, double qty) {
        require(Feature.EXPIRY);
        if (qty <= 0) throw new AgriException("Quantity must be greater than zero.");
        boolean any = cropFilter == null || cropFilter.equals("Any crop");
        double total = 0;
        for (StockItem s : traverseInventory())
            if ((any || s.crop.equalsIgnoreCase(cropFilter)) && !flagExpired(s)) total += Math.max(0, s.available());
        if (total + 0.0001 < qty) {
            logAuditTrail("INVALID_MOVEMENT", "FEFO", qty, 0, "FEFO dispatch rejected: only " + total + " kg available");
            throw new AgriException("Insufficient valid stock. Only " + String.format("%.2f", total) + " kg available.");
        }
        List<String> log = new ArrayList<>();
        DynamicArray<StockItem> held = new DynamicArray<>();
        double need = qty;
        while (need > 0.0001 && !expiryHeap.isEmpty()) {
            StockItem root = expiryHeap.peek();
            boolean skip = root.remaining <= 0 || flagExpired(root) || root.available() <= 0 || !(any || root.crop.equalsIgnoreCase(cropFilter));
            if (skip) {
                StockItem x = expiryHeap.extractMin();
                if (x.remaining > 0) held.add(x);                       // keep it, re-insert afterwards
                continue;
            }
            double take = Math.min(need, root.available());
            root.remaining -= take; root.dispatched += take; need -= take;
            log.add(String.format("%s (%s) - %.2f kg, expires %s", root.itemId, root.crop, take, root.expiryDate));
            logAuditTrail("DISPATCH", root.itemId, take, 0, "FEFO dispatch");
            if (root.remaining <= 0) expiryHeap.extractMin();
        }
        for (StockItem h : held) expiryHeap.insert(h);
        return log;
    }

    // MODULE 9: Sales & Stock Release (Queue, FIFO processing)
    public boolean checkAvailability(StockItem s, double qty) {
        return s != null && qty > 0 && !s.expiryDate.isBefore(today()) && qty <= s.available() + 0.0001;
    }

    public Sale createSale(String buyer, String itemId, double qty, double unitPrice, String ref) {
        require(Feature.SALES);
        if (buyer.trim().isEmpty()) throw new AgriException("Buyer name is required.");
        StockItem s = mustFindStock(itemId);
        if (unitPrice <= 0) throw new AgriException("Unit price must be greater than zero.");
        validateStockMovement(s, qty, "SALE");
        reserveStock(itemId, qty);
        Sale sale = new Sale();
        sale.ref = ref == null || ref.trim().isEmpty() ? String.format("SO-%04d", ++saleCounter) : ref.trim();
        if (sales.linearSearch(x -> x.ref.equalsIgnoreCase(sale.ref)) != null) { releaseReservation(itemId, qty); throw new AgriException("Reference number already used."); }
        sale.buyer = buyer.trim(); sale.itemId = itemId; sale.crop = s.crop; sale.qty = qty; sale.unitPrice = unitPrice; sale.date = today();
        sales.addFirst(sale);
        pendingSales.enqueue(sale);
        logAuditTrail("SALE", sale.ref, qty, 0, "Order created for " + sale.buyer + " (pending)");
        return sale;
    }

    // Confirms the order at the front of the FIFO queue; stock is reduced only now.
    public Sale confirmSale() {
        require(Feature.SALES);
        if (pendingSales.isEmpty()) throw new AgriException("No pending sales orders.");
        Sale sale = pendingSales.peek();
        StockItem s = mustFindStock(sale.itemId);
        if (sale.qty > s.remaining + 0.0001) throw new AgriException("Order " + sale.ref + " cannot be confirmed: stock level changed.");
        pendingSales.dequeue();
        s.reserved = Math.max(0, s.reserved - sale.qty);
        s.remaining -= sale.qty; s.sold += sale.qty;
        sale.status = "CONFIRMED";
        logAuditTrail("SALE", sale.ref, sale.qty, 0, "Sale confirmed - stock reduced");
        return sale;
    }

    public int processPendingSales() {
        int n = 0;
        while (!pendingSales.isEmpty()) { confirmSale(); n++; }
        return n;
    }

    public void cancelSale(Sale sale) {
        require(Feature.SALES);
        if (!sale.status.equals("PENDING")) throw new AgriException("Only pending orders can be cancelled.");
        pendingSales.remove(sale);
        StockItem s = stock.search(sale.itemId);
        if (s != null) s.reserved = Math.max(0, s.reserved - sale.qty);
        sale.status = "CANCELLED";
        logAuditTrail("SALE", sale.ref, sale.qty, 0, "Sale cancelled - reservation released");
    }

    public String generateReceipt(Sale s) {
        if (!s.status.equals("CONFIRMED")) throw new AgriException("Receipts are only issued for confirmed sales.");
        return "==========================================\n" +
               "            AGRISTOCK SALES RECEIPT\n" +
               "==========================================\n" +
               String.format("Reference No : %s%nDate         : %s%nBuyer        : %s%nBatch / Item  : %s%nCrop         : %s%n",
                       s.ref, s.date, s.buyer, s.itemId, s.crop) +
               String.format("Quantity     : %,.2f kg%nUnit price   : %s%n", s.qty, Fmt.peso(s.unitPrice)) +
               "------------------------------------------\n" +
               String.format("TOTAL        : %s%n", Fmt.peso(s.total())) +
               "==========================================\n" +
               "Issued by: " + (session == null ? "-" : session.fullName) + "\nThank you for your purchase!\n";
    }

    // MODULE 10: Dispatch & Delivery Monitoring (Priority Queue, Earliest Deadline First)
    public Delivery scheduleDelivery(Sale sale, LocalDate deadline, String vehicle, String destination, int urgency) {
        require(Feature.DISPATCH);
        if (sale == null || !sale.status.equals("CONFIRMED") || sale.deliveryScheduled) throw new AgriException("Select a confirmed sale that has no delivery yet.");
        if (deadline == null || deadline.isBefore(today())) throw new AgriException("Delivery deadline cannot be in the past.");
        if (vehicle.trim().isEmpty() || destination.trim().isEmpty()) throw new AgriException("Vehicle reference and destination are required.");
        Delivery d = new Delivery();
        d.id = String.format("DL-%04d", ++deliveryCounter);
        d.saleRef = sale.ref; d.company = sale.buyer; d.itemId = sale.itemId; d.qty = sale.qty;
        d.deadline = deadline; d.vehicle = vehicle.trim(); d.destination = destination.trim(); d.urgency = urgency;
        sale.deliveryScheduled = true;
        deliveries.addFirst(d);
        deliveryQueue.enqueue(d);                                      // heapify-up by deadline
        logAuditTrail("DISPATCH", d.id, d.qty, 0, "Delivery scheduled for " + d.company + " by " + deadline);
        return d;
    }

    public DynamicArray<Delivery> getUpcomingDeliveries() { return deliveryQueue.sortedSnapshot(); }

    // Dispatches the delivery with the earliest deadline (heap root).
    public Delivery recordDispatch() {
        require(Feature.DISPATCH);
        if (deliveryQueue.isEmpty()) throw new AgriException("No scheduled deliveries.");
        Delivery d = deliveryQueue.dequeue();
        d.status = "DISPATCHED"; d.dispatchedQty = d.qty; d.dispatchDate = today();
        StockItem s = stock.search(d.itemId);
        if (s != null) s.dispatched += d.qty;
        logAuditTrail("DISPATCH", d.id, d.qty, 0, "Dispatched to " + d.company + " via " + d.vehicle);
        return d;
    }

    public void confirmReceipt(Delivery d, double receivedQty) {
        require(Feature.DISPATCH);
        if (d == null || !d.status.equals("DISPATCHED")) throw new AgriException("Select a dispatched delivery.");
        if (receivedQty < 0 || receivedQty > d.dispatchedQty + 0.0001) throw new AgriException("Received quantity must be between 0 and the dispatched quantity.");
        d.receivedQty = receivedQty; d.receivedDate = today(); d.status = "RECEIVED";
        logAuditTrail("DISPATCH", d.id, d.dispatchedQty, receivedQty, "Receipt confirmed");
        if (receivedQty < d.dispatchedQty) reconcileShortage(d);
    }

    public void reconcileShortage(Delivery d) {
        double shortage = d.dispatchedQty - d.receivedQty;
        StockItem s = stock.search(d.itemId);
        Spoilage sp = newSpoilage(d.itemId, shortage, "Delivery shortage on " + d.id, session == null ? "SYSTEM" : session.username);
        sp.kind = "Delivery shortage"; sp.status = "APPROVED"; sp.approver = sp.reporter;
        sp.lossValue = shortage * (s == null ? 0 : s.unitValue);
        logAuditTrail("ADJUSTMENT", d.id, d.dispatchedQty, d.receivedQty, "Shortage of " + shortage + " kg reconciled as loss");
    }

    // MODULE 11: Spoilage, Loss & Adjustments (Linked List, append-only)
    private Spoilage newSpoilage(String itemId, double qty, String reason, String reporter) {
        Spoilage sp = new Spoilage();
        sp.id = String.format("SP-%04d", ++spoilageCounter);
        sp.itemId = itemId; sp.qty = qty; sp.reason = reason; sp.reporter = reporter; sp.date = today();
        spoilage.addFirst(sp);
        return sp;
    }

    // requestAdjustment: logs the loss; authorised users (owner/manager) approve immediately.
    public Spoilage recordSpoilage(String itemId, double qty, String reason, String reporter) {
        require(Feature.SPOILAGE_LOG);
        StockItem s = mustFindStock(itemId);
        if (reason == null || reason.trim().isEmpty()) throw new AgriException("A reason is required.");
        validateStockMovement(s, qty, "SPOILAGE");
        Spoilage sp = newSpoilage(itemId, qty, reason.trim(), reporter.trim().isEmpty() ? session.username : reporter.trim());
        logAuditTrail("ADJUSTMENT", sp.id, qty, 0, "Spoilage reported: " + reason);
        if (checkPermission(Feature.SPOILAGE_APPROVE)) approveAdjustment(sp);
        return sp;
    }

    public void approveAdjustment(Spoilage sp) {
        require(Feature.SPOILAGE_APPROVE);
        if (!sp.status.equals("PENDING")) throw new AgriException("Only pending requests can be approved.");
        StockItem s = mustFindStock(sp.itemId);
        validateStockMovement(s, sp.qty, "SPOILAGE");
        s.remaining -= sp.qty; s.damaged += sp.qty;
        logFinancialLoss(sp, s);
        sp.status = "APPROVED"; sp.approver = session.username;
        logAuditTrail("ADJUSTMENT", sp.id, sp.qty, 0, "Adjustment approved by " + session.username);
    }

    public void rejectAdjustment(Spoilage sp) {
        require(Feature.SPOILAGE_APPROVE);
        if (!sp.status.equals("PENDING")) throw new AgriException("Only pending requests can be rejected.");
        sp.status = "REJECTED"; sp.approver = session.username;
        logAuditTrail("ADJUSTMENT", sp.id, sp.qty, 0, "Adjustment rejected by " + session.username);
    }

    public void logFinancialLoss(Spoilage sp, StockItem s) { sp.lossValue = sp.qty * s.unitValue; }

    // Linear traversal of the list from the head, aggregating approved losses.
    public String getFinancialLossReport() {
        StringBuilder sb = new StringBuilder("FINANCIAL LOSS REPORT (approved entries)\n----------------------------------------\n");
        double qty = 0, value = 0;
        for (Spoilage sp : spoilage) {
            if (!sp.status.equals("APPROVED")) continue;
            sb.append(String.format("%s | %s | %-17s | %,.2f kg | %s | %s%n", sp.id, sp.itemId, sp.kind, sp.qty, Fmt.peso(sp.lossValue), sp.reason));
            qty += sp.qty; value += sp.lossValue;
        }
        sb.append(String.format("----------------------------------------%nTotal quantity lost : %,.2f kg%nTotal financial loss: %s%n", qty, Fmt.peso(value)));
        return sb.toString();
    }

    // MODULE 14: Inventory Audit & Validation (append-only Linked List, linear search)
    public void logAuditTrail(String type, String ref, double recorded, double physical, String note) {
        AuditEntry e = new AuditEntry();
        e.type = type; e.ref = ref; e.recorded = recorded; e.physical = physical; e.discrepancy = physical - recorded;
        if (!type.equals("AUDIT_COUNT") && !type.equals("AUDIT_RECONCILE")) e.discrepancy = 0;
        e.note = note; e.time = LocalDateTime.now(); e.user = session == null ? "SYSTEM" : session.username;
        audit.addFirst(e);
    }

    public double compareCounts(String itemId, double physical) {
        require(Feature.AUDIT_VIEW);
        StockItem s = mustFindStock(itemId);
        if (physical < 0) throw new AgriException("Physical count cannot be negative.");
        return physical - s.remaining;
    }

    public void recordDifference(String itemId, double physical, String note) {
        double diff = compareCounts(itemId, physical);
        logAuditTrail("AUDIT_COUNT", itemId, physical - diff, physical, note.isBlank() ? "Physical count recorded" : note.trim());
    }

    public void reconcile(String itemId, double physical, String note) {
        if (!checkPermission(Feature.AUDIT_RECONCILE)) {
            logAuditTrail("INVALID_MOVEMENT", itemId, 0, physical, "Unauthorised reconciliation attempt");
            throw new AgriException("Authorization required for inventory count reconciliation (Farm Owner / Farm Manager).");
        }
        StockItem s = mustFindStock(itemId);
        double diff = compareCounts(itemId, physical);
        if (physical < s.reserved - 0.0001) throw new AgriException("Physical count is lower than the reserved quantity (" + s.reserved + " kg). Resolve pending sales first.");
        double recorded = s.remaining;
        s.remaining = physical;
        if (physical > 0 && diff > 0) rebuildExpiryHeap();
        logAuditTrail("AUDIT_RECONCILE", itemId, recorded, physical, note.isBlank() ? "Count reconciled" : note.trim());
    }

    // Rejects impossible stock movements and logs them in the audit trail.
    public void validateStockMovement(StockItem s, double qty, String type) {
        String err = null;
        if (qty <= 0) err = "Quantity must be greater than zero.";
        else if (type.equals("SALE") && s.expiryDate.isBefore(today())) err = "Batch " + s.itemId + " is expired and cannot be sold.";
        else if (qty > s.available() + 0.0001) err = "Only " + String.format("%.2f", s.available()) + " kg of " + s.itemId + " is available (rest is reserved or sold).";
        if (err != null) {
            logAuditTrail("INVALID_MOVEMENT", s.itemId, s.remaining, 0, type + " rejected: " + err);
            throw new AgriException(err);
        }
    }

    // Linear search through the audit trail for one batch/reference.
    public List<AuditEntry> auditHistory(String ref) {
        List<AuditEntry> out = new ArrayList<>();
        for (AuditEntry e : audit) if (ref == null || ref.isBlank() || e.ref.equalsIgnoreCase(ref.trim())) out.add(e);
        return out;
    }
}
