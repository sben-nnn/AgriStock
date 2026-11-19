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

    protected void header(String heading, String sub) {
        getChildren().addAll(UI.heading(heading), UI.subtitle(sub));
    }
}