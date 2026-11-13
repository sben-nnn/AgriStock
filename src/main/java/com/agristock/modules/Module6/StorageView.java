package com.agristock.modules.Module6;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;
import com.agristock.ui.Ctx;

// MODULE 6: Storage & Location Management (Array + FIFO Queue rotation)
public class StorageView extends View {
    public StorageView(Ctx ctx) { super(ctx); }

    @Override public String title() { return "6  Storage"; }
    @Override public Feature feature() { return Feature.STORAGE; }
    @Override public void refresh() { getChildren().clear(); header("Storage & Location", "Assign storage locations and rotate stock FIFO."); }
}
