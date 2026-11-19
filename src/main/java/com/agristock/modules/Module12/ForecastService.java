package com.agristock.modules.Module12;

import com.agristock.model.AgriException;
import com.agristock.model.Models.Feature;
import com.agristock.model.Models.YieldRecord;
import com.agristock.service.AgriStockSystem;

public class ForecastService {
    private final AgriStockSystem sys;
    public ForecastService(AgriStockSystem sys) { this.sys = sys; }

    public void addYieldRecord(String crop, String season, double yield, double demand) {
        if (!sys.checkPermission(Feature.FORECAST)) throw new AgriException("Access denied for your role.");
        if (crop == null || crop.trim().isEmpty() || season.trim().isEmpty()) throw new AgriException("Crop and season label are required.");
        if (yield <= 0 || demand < 0) throw new AgriException("Yield must be positive and demand cannot be negative.");
        sys.yields.add(new YieldRecord(crop.trim(), season.trim(), yield, demand));
    }

    public double[] yieldsFor(String crop, boolean demand) {
        int n = 0;
        for (YieldRecord y : sys.yields) if (y.crop.equalsIgnoreCase(crop)) n++;
        double[] out = new double[n];
        int i = 0;
        for (YieldRecord y : sys.yields) if (y.crop.equalsIgnoreCase(crop)) out[i++] = demand ? y.demand : y.yield;
        return out;
    }

    public static double movingAverage(double[] data, int window) {
        if (window <= 0 || data.length < window)
            throw new AgriException("Insufficient historical data points for a window of " + window + " seasons.");
        double sum = 0;
        for (int i = data.length - window; i < data.length; i++) sum += data[i];
        return sum / window;
    }

    public static double forecastSeedNeed(double[] yields, int window, double seedRatio) {
        return movingAverage(yields, window) * seedRatio;
    }

    public static double forecastSeedBudget(double[] yields, int window, double seedRatio, double costPerKg) {
        return forecastSeedNeed(yields, window, seedRatio) * costPerKg;
    }
}