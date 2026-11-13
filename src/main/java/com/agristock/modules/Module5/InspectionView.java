package com.agristock.modules.Module5;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;

// View for inspection screen
public class InspectionView extends View {
    public InspectionView() { super(null); }

    @Override public String title() { return "5  Inspection"; }
    @Override public Feature feature() { return Feature.INSPECTION; }
    @Override public void refresh() { getChildren().clear(); }
}
