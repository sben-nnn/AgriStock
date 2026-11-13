package com.agristock.modules.Module13;

import com.agristock.ds.DynamicArray;
import com.agristock.model.Models.ReportEntry;
import com.agristock.service.AgriStockSystem;
import com.agristock.service.ReportService;

import java.time.LocalDate;

public class ReportsService {
    private final AgriStockSystem sys;
    private final ReportService rep;
    public ReportsService(AgriStockSystem sys, ReportService rep) { this.sys = sys; this.rep = rep; }

    public DynamicArray<ReportEntry> generateReport(String type, String crop, String farm, String batchId, LocalDate from, LocalDate to) {
        return rep.generateReport(type, crop, farm, batchId, from, to);
    }

    public String computeEstimatedRevenue() {
        return rep.computeEstimatedRevenue();
    }

    public String identifyHighLossCrops() {
        return rep.identifyHighLossCrops();
    }

    public String identifySlowMoving() {
        return rep.identifySlowMoving();
    }

    public String analyzeSeasonalTrends() {
        return rep.analyzeSeasonalTrends();
    }
}
