package com.agristock.ui;

import com.agristock.model.Models.Feature;

// Batches placeholder
public class BatchesView extends View {
    public BatchesView(Ctx ctx) { super(ctx); }
    @Override public String title() { return "3  Batches"; }
    @Override public Feature feature() { return Feature.BATCHES; }
    @Override public void refresh() { getChildren().clear(); header("Harvest Batches", "Register and track harvest batches."); }
}
