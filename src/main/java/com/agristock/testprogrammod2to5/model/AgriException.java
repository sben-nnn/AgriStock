package com.agristock.testprogrammod2to5.model;

// Thrown for any validation / permission failure; the GUI shows the message in an alert.
public class AgriException extends RuntimeException {
    public AgriException(String message) { super(message); }
}
