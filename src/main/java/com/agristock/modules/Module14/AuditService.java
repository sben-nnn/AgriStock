package com.agristock.modules.Module14;

import com.agristock.model.Models.AuditEntry;
import com.agristock.service.AgriStockSystem;

import java.util.List;

public class AuditService{
    private final AgriStockSystem sys;
    public AuditService(AgriStockSystem sys) { this.sys = sys; }

    public double compareCounts(String itemId, double physical) { return sys.compareCounts(itemId, physical); }

    public void recordDifference(String itemId, double physical, String note) { sys.recordDifference(itemId, physical, note); }

    public void reconcile(String itemId, double physical, String note) { sys.reconcile(itemId, physical, note); }

    public void validateStockMovement(String itemId, double qty, String type) {
        sys.validateStockMovement(sys.mustFindStock(itemId), qty, type);
    }

    public void logAuditTrail(String type, String ref, double recorded, double physical, String note) {
        sys.logAuditTrail(type, ref, recorded, physical, note);
    }

    public List<AuditEntry> auditHistory(String ref) { return sys.auditHistory(ref); }
}
