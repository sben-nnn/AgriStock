package com.agristock.modules.Module12;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;
import com.agristock.ui.Ctx;

// MODULE 12: Forecasting (array + moving average)
public class ForecastView extends View {
    public ForecastView(Ctx ctx) { super(ctx); }

    @Override public String title() { return "12  Forecasting"; }
    @Override public Feature feature() { return Feature.FORECAST; }
    @Override public void refresh() { getChildren().clear(); header("Forecasting", "Historical yields and moving average seed need calculation."); }
}
