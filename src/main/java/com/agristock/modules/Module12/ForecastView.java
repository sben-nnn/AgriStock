package com.agristock.modules.Module12;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;

// View for forecast screen
public class ForecastView extends View {
    public ForecastView() { super(null); }

    @Override public String title() { return "12  Forecasting"; }
    @Override public Feature feature() { return Feature.FORECAST; }
    @Override public void refresh() { getChildren().clear(); }
}
