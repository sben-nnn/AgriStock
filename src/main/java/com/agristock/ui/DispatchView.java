package com.agristock.ui;

import com.agristock.model.Models.Feature;

// Dispatch placeholder
public class DispatchView extends View {
    public DispatchView(Ctx ctx) { super(ctx); }
    @Override public String title() { return "10  Dispatch"; }
    @Override public Feature feature() { return Feature.DISPATCH; }
    @Override public void refresh() { getChildren().clear(); header("Dispatch & Delivery", "Schedule deliveries by earliest deadline and confirm receipt."); }
}
