package com.agristock.modules.Module9;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;

// View for sales screen
public class SalesView extends View {
    public SalesView() { super(null); }

    @Override public String title() { return "9  Sales"; }
    @Override public Feature feature() { return Feature.SALES; }
    @Override public void refresh() { getChildren().clear(); }
}
