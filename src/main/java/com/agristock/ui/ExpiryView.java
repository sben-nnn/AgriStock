package com.agristock.ui;

import com.agristock.model.Models.Feature;

// Expiry placeholder
public class ExpiryView extends View {
    public ExpiryView(Ctx ctx) { super(ctx); }
    @Override public String title() { return "8  Expiry"; }
    @Override public Feature feature() { return Feature.EXPIRY; }
    @Override public void refresh() { getChildren().clear(); header("Expiry & Purge", "FEFO dispatch and expired stock purge."); }
}
