package com.agristock.service;

/** Small formatting helpers shared by services and the GUI. */
public final class Fmt {
    private Fmt() {}
    public static String peso(double v) { return String.format("\u20B1%,.2f", v); }
    public static String kg(double v) { return String.format("%,.2f", v); }
}
