package com.agristock.ui;

import com.agristock.model.Models.Feature;
import com.agristock.service.AgriStockSystem;
import javafx.scene.layout.VBox;

public abstract class View extends VBox {
    protected final Ctx ctx;
    protected final AgriStockSystem sys;

    protected View(Ctx ctx) {
        this.ctx = ctx; this.sys = ctx.sys;
        getStyleClass().add("page");
    }

    public abstract String title();
    public Feature feature() { return null; }
    public abstract void refresh();

    /** Refresh every view so all module tables stay in sync after any data change. */
    protected void refreshAll() { ctx.reloadAll.run(); }

    protected void header(String heading, String sub) {
        getChildren().add(UI.heading(heading));
        if (sub != null && !sub.isBlank()) getChildren().add(UI.subtitle(sub));
    }
}