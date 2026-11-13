package com.agristock.modules.Module11;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;

// View for spoilage screen
public class SpoilageView extends View {
    public SpoilageView() { super(null); }

    @Override public String title() { return "11  Spoilage & Loss"; }
    @Override public Feature feature() { return Feature.SPOILAGE_LOG; }
    @Override public void refresh() { getChildren().clear(); }
}
