package com.agristock.modules.Module11;

import com.agristock.model.Models.Spoilage;
import com.agristock.model.Models.StockItem;
import com.agristock.service.AgriStockSystem;

import java.util.ArrayList;
import java.util.List;

public class SpoilageService {
    private final AgriStockSystem sys;
    public SpoilageService(AgriStockSystem sys) { this.sys = sys; }

    public Spoilage recordSpoilage(String itemId, double qty, String reason, String reporter) {
        return sys.recordSpoilage(itemId, qty, reason, reporter);
    }

    public Spoilage requestAdjustment(String itemId, double qty, String reason, String reporter) {
        return sys.recordSpoilage(itemId, qty, reason, reporter);
    }

    public void approveAdjustment(Spoilage sp) { sys.approveAdjustment(sp); }

    public void rejectAdjustment(Spoilage sp) { sys.rejectAdjustment(sp); }

    public void logFinancialLoss(Spoilage sp, StockItem item) { sys.logFinancialLoss(sp, item); }

    public String getFinancialLossReport() { return sys.getFinancialLossReport(); }

    public List<Spoilage> findByBatch(String itemId) { return sys.spoilageForBatch(itemId); }

    public List<Spoilage> pendingRequests() {
        List<Spoilage> out = new ArrayList<>();
        for (Spoilage sp : sys.spoilage) if (sp.status.equals("PENDING")) out.add(sp);
        return out;
    }
}
