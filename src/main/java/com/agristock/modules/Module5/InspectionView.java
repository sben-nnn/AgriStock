package com.agristock.modules.Module5;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;
import com.agristock.ui.Ctx;

// MODULE 5: Quality Inspection & Grading (Queue, FIFO scheduling)
public class InspectionView extends View {
    public InspectionView(Ctx ctx) { super(ctx); }

    @Override public String title() { return "5  Inspection"; }
    @Override public Feature feature() { return Feature.INSPECTION; }
    @Override public void refresh() { getChildren().clear(); header("Quality Inspection", "Inspect and grade batches awaiting quality check."); }
}
