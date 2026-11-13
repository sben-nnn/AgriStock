package com.agristock.modules.Module13;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;
import com.agristock.ui.Ctx;

// MODULE 13: Reports & Analytics (array, merge sort, binary search on date ranges)
public class ReportsView extends View {
    public ReportsView(Ctx ctx) { super(ctx); }

    @Override public String title() { return "13  Reports & Analytics"; }
    @Override public Feature feature() { return Feature.REPORTS; }
    @Override public void refresh() { getChildren().clear(); header("Reports & Analytics", ""); }
}
