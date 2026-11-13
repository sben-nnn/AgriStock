package com.agristock.ui;

import com.agristock.model.Models.Feature;

// Inspection placeholder
public class InspectionView extends View {
    public InspectionView(Ctx ctx) { super(ctx); }
    @Override public String title() { return "5  Inspection"; }
    @Override public Feature feature() { return Feature.INSPECTION; }
    @Override public void refresh() { getChildren().clear(); header("Quality Inspection", "Inspect and grade batches awaiting quality check."); }
}
