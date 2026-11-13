package com.agristock.ui;

import com.agristock.model.Models.Feature;

// Sales placeholder
public class SalesView extends View {
    public SalesView(Ctx ctx) { super(ctx); }
    @Override public String title() { return "9  Sales"; }
    @Override public Feature feature() { return Feature.SALES; }
    @Override public void refresh() { getChildren().clear(); header("Sales & Stock Release", "Create sales orders and confirm them from the FIFO queue."); }
}
