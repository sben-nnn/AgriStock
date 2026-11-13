package com.agristock.modules.Module8;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;

// View for expiry screen
public class ExpiryView extends View {
    public ExpiryView() { super(null); }

    @Override public String title() { return "8  Expiry"; }
    @Override public Feature feature() { return Feature.EXPIRY; }
    @Override public void refresh() { getChildren().clear(); }
}
