package com.agristock.testprogrammod2to5.ui;

import com.agristock.testprogrammod2to5.model.Models.Feature;

// Dashboard - shows quick summary stats
public class DashboardView extends View {
    public DashboardView(Ctx ctx) { super(ctx); }
    @Override public String title() { return "0  Dashboard"; }
    @Override public Feature feature() { return null; }
    @Override public void refresh() { getChildren().clear(); header("Dashboard", "AVL Tree in-order traversal for inventory; Min-Heap + Priority Queue for deliveries"); }
}
