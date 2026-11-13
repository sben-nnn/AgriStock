package com.agristock.modules.Module11;

import com.agristock.model.Models.Spoilage;
import com.agristock.service.AgriStockSystem;

public class SpoilageService {
    private final AgriStockSystem sys;
    public SpoilageService(AgriStockSystem sys) { this.sys = sys; }

    public Spoilage recordSpoilage(String itemId, double qty, String reason, String reporter) {
        return sys.recordSpoilage(itemId, qty, reason, reporter);
    }

    public void approveAdjustment(Spoilage sp) {
        sys.approveAdjustment(sp);
    }

    public void rejectAdjustment(Spoilage sp) {
        sys.rejectAdjustment(sp);
    }

    public String getFinancialLossReport() {
        return sys.getFinancialLossReport();
    }
}
