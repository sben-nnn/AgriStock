package com.agristock.ui;

import com.agristock.model.Models.Feature;

// Storage placeholder
public class StorageView extends View {
    public StorageView(Ctx ctx) { super(ctx); }
    @Override public String title() { return "6  Storage"; }
    @Override public Feature feature() { return Feature.STORAGE; }
    @Override public void refresh() { getChildren().clear(); header("Storage & Location", "Assign storage locations and rotate stock FIFO."); }
}
