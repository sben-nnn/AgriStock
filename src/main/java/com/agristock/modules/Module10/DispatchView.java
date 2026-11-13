package com.agristock.modules.Module10;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;

// View for dispatch screen
public class DispatchView extends View {
    public DispatchView() { super(null); }

    @Override public String title() { return "10  Dispatch"; }
    @Override public Feature feature() { return Feature.DISPATCH; }
    @Override public void refresh() { getChildren().clear(); }
}
