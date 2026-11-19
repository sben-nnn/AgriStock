package com.agristock.ui;

import com.agristock.service.AgriStockSystem;

public class Ctx {
    public final AgriStockSystem sys;
    public final Runnable reloadAll;
    public Ctx(AgriStockSystem sys, Runnable reloadAll) { this.sys = sys; this.reloadAll = reloadAll; }
}