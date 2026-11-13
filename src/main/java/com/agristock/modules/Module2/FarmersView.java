package com.agristock.modules.Module2;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;

// View for farmers screen
public class FarmersView extends View {
    public FarmersView() { super(null); }

    @Override public String title() { return "2  Farmers"; }
    @Override public Feature feature() { return Feature.FARMERS; }
    @Override public void refresh() { getChildren().clear(); }
}
