package com.agristock.modules.Module12;

import com.agristock.model.Models.YieldRecord;
import com.agristock.service.AgriStockSystem;
import com.agristock.service.ReportService;

/** MODULE 12: Forecasting service (array + moving average). */
public class ForecastService {
    private final AgriStockSystem sys;
    private final ReportService rep;
    public ForecastService(AgriStockSystem sys, ReportService rep) { this.sys = sys; this.rep = rep; }

    public void addYieldRecord(String crop, String season, double yield, double demand) {
        rep.addYieldRecord(crop, season, yield, demand);
    }

    public double[] yieldsFor(String crop, boolean demand) {
        return rep.yieldsFor(crop, demand);
    }

    public static double movingAverage(double[] data, int window) {
        return ReportService.movingAverage(data, window);
    }

    public static double forecastSeedNeed(double[] yields, int window, double seedRatio) {
        return ReportService.forecastSeedNeed(yields, window, seedRatio);
    }
}
