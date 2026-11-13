package com.agristock.ui;

import com.agristock.model.Models.Feature;

// Harvest stock placeholder
public class HarvestStockView extends View {
    public HarvestStockView(Ctx ctx) { super(ctx); }
    @Override public String title() { return "4  Harvest Stock"; }
    @Override public Feature feature() { return Feature.HARVEST_STOCK; }
    @Override public void refresh() { getChildren().clear(); header("Harvest Stock", "Record harvested stock and quality inspections."); }
}
