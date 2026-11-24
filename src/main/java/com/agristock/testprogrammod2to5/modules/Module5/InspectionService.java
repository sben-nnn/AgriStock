package com.agristock.testprogrammod2to5.modules.Module5;

import com.agristock.testprogrammod2to5.model.Models.BatchStatus;
import com.agristock.testprogrammod2to5.service.AgriStockSystem;

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
