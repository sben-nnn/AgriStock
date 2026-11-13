package com.agristock.modules.Module14;

import com.agristock.model.Models.Feature;
import com.agristock.ui.View;

// View for audit screen
public class AuditView extends View {
    public AuditView() { super(null); }

    @Override public String title() { return "14  Audit & Validation"; }
    @Override public Feature feature() { return Feature.AUDIT_VIEW; }
    @Override public void refresh() { getChildren().clear(); }
}
