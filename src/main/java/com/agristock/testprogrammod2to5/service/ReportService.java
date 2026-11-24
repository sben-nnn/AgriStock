package com.agristock.testprogrammod2to5.service;

import com.agristock.testprogrammod2to5.model.AgriException;
import com.agristock.testprogrammod2to5.model.Models.*;

/** Reports and analytics service. */
public class ReportService {
    private final AgriStockSystem sys;

    public ReportService(AgriStockSystem s) { this.sys = s; }

    public void addYieldRecord(String crop, String season, double yield, double demand) {
        sys.yields.add(new YieldRecord(crop, season, yield, demand));
    }
}
