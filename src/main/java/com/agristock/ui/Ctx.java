package com.agristock.ui;

import com.agristock.service.AgriStockSystem;
import com.agristock.service.ReportService;

/** Shared context handed to every view. */
public class Ctx {
    public final AgriStockSystem sys;
    public final ReportService rep;
    public final Runnable reloadAll;   // called after sample data is loaded
    public Ctx(AgriStockSystem sys, ReportService rep, Runnable reloadAll) { this.sys = sys; this.rep = rep; this.reloadAll = reloadAll; }
}
