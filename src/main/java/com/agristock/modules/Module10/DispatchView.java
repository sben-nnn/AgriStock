package com.agristock.modules.Module10;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;
import com.agristock.ui.Ctx;

// MODULE 10: Dispatch & Delivery Monitoring (Priority Queue, Earliest Deadline First)
public class DispatchView extends View {
    public DispatchView(Ctx ctx) { super(ctx); }

    @Override public String title() { return "10  Dispatch"; }
    @Override public Feature feature() { return Feature.DISPATCH; }
    @Override public void refresh() { getChildren().clear(); header("Dispatch & Delivery", "Schedule deliveries by earliest deadline and confirm receipt."); }
}
