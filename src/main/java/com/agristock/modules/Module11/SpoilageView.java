package com.agristock.modules.Module11;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;
import com.agristock.ui.Ctx;

// MODULE 11: Spoilage, Loss & Adjustments (Linked List, append-only)
public class SpoilageView extends View {
    public SpoilageView(Ctx ctx) { super(ctx); }

    @Override public String title() { return "11  Spoilage & Loss"; }
    @Override public Feature feature() { return Feature.SPOILAGE_LOG; }
    @Override public void refresh() { getChildren().clear(); header("Spoilage & Loss", "Log spoilage, approve/reject adjustments, track financial loss."); }
}
