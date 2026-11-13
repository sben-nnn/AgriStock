package com.agristock.modules.Module6;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;

// View for storage screen
public class StorageView extends View {
    public StorageView() { super(null); }

    @Override public String title() { return "6  Storage"; }
    @Override public Feature feature() { return Feature.STORAGE; }
    @Override public void refresh() { getChildren().clear(); }
}
