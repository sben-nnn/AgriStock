package com.agristock.modules.Module5;

import com.agristock.model.Models.BatchStatus;
import com.agristock.service.AgriStockSystem;

// Service for managing batch inspections
public class InspectionService {
    private final AgriStockSystem sys;

    public InspectionService(AgriStockSystem sys) {
        this.sys = sys;
    }

    public void inspect(BatchStatus status, String grade, String notes, double unitValue) {
        sys.inspectBatch(status.toString(), grade, notes, unitValue);
    }
}
