package com.agristock.modules.Module14;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;
import com.agristock.ui.Ctx;

// MODULE 14: Inventory Audit & Validation (append-only Linked List, linear search)
public class AuditView extends View {
    public AuditView(Ctx ctx) { super(ctx); }

    @Override public String title() { return "14  Audit & Validation"; }
    @Override public Feature feature() { return Feature.AUDIT_VIEW; }
    @Override public void refresh() { getChildren().clear(); header("Audit & Validation", "Compare physical counts with recorded stock."); }
}
