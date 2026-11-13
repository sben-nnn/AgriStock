package com.agristock.modules.Module14;

import com.agristock.model.Models.AuditEntry;
import com.agristock.service.AgriStockSystem;

import java.util.List;

/** MODULE 14: Inventory Audit & Validation service (append-only linked list + linear search). */
public class AuditService {
    private final AgriStockSystem sys;
    public AuditService(AgriStockSystem sys) { this.sys = sys; }

    public double compareCounts(String itemId, double physical) {
        return sys.compareCounts(itemId, physical);
    }

    public void recordDifference(String itemId, double physical, String note) {
        sys.recordDifference(itemId, physical, note);
    }

    public void reconcile(String itemId, double physical, String note) {
        sys.reconcile(itemId, physical, note);
    }

    public List<AuditEntry> auditHistory(String ref) {
        return sys.auditHistory(ref);
    }
}
