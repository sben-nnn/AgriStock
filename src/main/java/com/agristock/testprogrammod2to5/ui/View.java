package com.agristock.testprogrammod2to5.ui;

import com.agristock.testprogrammod2to5.model.Models.Feature;
import com.agristock.testprogrammod2to5.service.AgriStockSystem;
import com.agristock.testprogrammod2to5.service.ReportService;
import javafx.scene.layout.VBox;

/** One screen (module) of the application. */
public abstract class View extends VBox {
    protected final Ctx ctx;
    protected final AgriStockSystem sys;
    protected final ReportService rep;

    protected View(Ctx ctx) {
        this.ctx = ctx; this.sys = ctx.sys; this.rep = ctx.rep;
        getStyleClass().add("page");
    }

    /** Navigation label, e.g. "3  Batch Registration". */
    public abstract String title();
    /** Feature needed to see this screen (null = everyone). */
    public Feature feature() { return null; }
    /** Reload all controls from the data structures. */
    public abstract void refresh();

    protected void header(String heading, String sub) {
        getChildren().addAll(UI.heading(heading), UI.subtitle(sub));
    }
}
