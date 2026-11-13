package com.agristock.modules.Module8;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;
import com.agristock.ui.Ctx;

// MODULE 8: Expiry & Purge (Min-Heap, FEFO / extract-min)
public class ExpiryView extends View {
    public ExpiryView(Ctx ctx) { super(ctx); }

    @Override public String title() { return "8  Expiry"; }
    @Override public Feature feature() { return Feature.EXPIRY; }
    @Override public void refresh() { getChildren().clear(); header("Expiry & Purge", ""); }
}
