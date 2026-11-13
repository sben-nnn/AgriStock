package com.agristock.ui;

import com.agristock.model.Models.Feature;

// Dashboard - shows quick summary stats
public class DashboardView extends View {
    public DashboardView(Ctx ctx) { super(ctx); }
    @Override public String title() { return "0  Dashboard"; }
    @Override public Feature feature() { return null; }
    @Override public void refresh() { getChildren().clear(); header("Dashboard", ""); }
}
