package com.agristock.modules.Module7;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;

// View for stock screen
public class StockView extends View {
    public StockView() { super(null); }

    @Override public String title() { return "7  Stock"; }
    @Override public Feature feature() { return Feature.STOCK_EDIT; }
    @Override public void refresh() { getChildren().clear(); }
}
