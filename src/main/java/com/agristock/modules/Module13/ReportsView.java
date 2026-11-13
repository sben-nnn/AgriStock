package com.agristock.modules.Module13;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;

// View for reports screen
public class ReportsView extends View {
    public ReportsView() { super(null); }

    @Override public String title() { return "13  Reports & Analytics"; }
    @Override public Feature feature() { return Feature.REPORTS; }
    @Override public void refresh() { getChildren().clear(); }
}
