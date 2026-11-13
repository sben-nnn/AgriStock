package com.agristock.modules.Module7;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;
import com.agristock.ui.Ctx;

// MODULE 7: Stock Monitoring & Control (AVL Tree, in-order traversal, binary search)
public class StockView extends View {
    public StockView(Ctx ctx) { super(ctx); }

    @Override public String title() { return "7  Stock"; }
    @Override public Feature feature() { return Feature.STOCK_EDIT; }
    @Override public void refresh() { getChildren().clear(); header("Stock Monitoring", ""); }
}
