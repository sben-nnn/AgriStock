package com.agristock.modules.Module9;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;
import com.agristock.ui.Ctx;

// MODULE 9: Sales & Stock Release (Queue, FIFO processing)
public class SalesView extends View {
    public SalesView(Ctx ctx) { super(ctx); }

    @Override public String title() { return "9  Sales"; }
    @Override public Feature feature() { return Feature.SALES; }
    @Override public void refresh() { getChildren().clear(); header("Sales & Stock Release", "Create sales orders and confirm them from the FIFO queue."); }
}
