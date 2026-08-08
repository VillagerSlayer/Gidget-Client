package com.gidget.client.macro;

/** A named keybind that sends a chat message (or, prefixed with '/', a command) when pressed. */
public final class Macro {
    public String name;
    public int keyCode;
    public String message;

    public Macro(String name, int keyCode, String message) {
        this.name = name;
        this.keyCode = keyCode;
        this.message = message;
    }
}
