package com.agristock.modules.Module4;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;

// View for harvest stock screen
public class HarvestStockView extends View {
    public HarvestStockView() { super(null); }

    @Override public String title() { return "4  Harvest Stock"; }
    @Override public Feature feature() { return Feature.HARVEST_STOCK; }
    @Override public void refresh() { getChildren().clear(); }
}
