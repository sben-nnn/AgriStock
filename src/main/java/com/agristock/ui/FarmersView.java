package com.agristock.ui;

import com.agristock.model.Models.Feature;

// Farmers placeholder
public class FarmersView extends View {
    public FarmersView(Ctx ctx) { super(ctx); }
    @Override public String title() { return "2  Farmers"; }
    @Override public Feature feature() { return Feature.FARMERS; }
    @Override public void refresh() { getChildren().clear(); header("Farmers", "Farmer registration and harvest tracking."); }
}
