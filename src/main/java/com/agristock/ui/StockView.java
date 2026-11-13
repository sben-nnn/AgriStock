package com.agristock.ui;

import com.agristock.model.Models.Feature;

// Stock placeholder
public class StockView extends View {
    public StockView(Ctx ctx) { super(ctx); }
    @Override public String title() { return "7  Stock"; }
    @Override public Feature feature() { return Feature.STOCK_EDIT; }
    @Override public void refresh() { getChildren().clear(); header("Stock Monitoring", "Monitor stock levels, low stock, aging, and expiry flags."); }
}
