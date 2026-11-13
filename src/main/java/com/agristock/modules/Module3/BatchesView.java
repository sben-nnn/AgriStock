package com.agristock.modules.Module3;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;

// View for batches screen
public class BatchesView extends View {
    public BatchesView() { super(null); }

    @Override public String title() { return "3  Batches"; }
    @Override public Feature feature() { return Feature.BATCHES; }
    @Override public void refresh() { getChildren().clear(); }
}
