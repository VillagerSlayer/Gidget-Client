package com.gidget.client.gui;

/** Colors for the ClickGUI: a red variant of Meteor Client's real multi-column layout. */
public final class GidgetTheme {
    private GidgetTheme() {
    }

    public static final int DEFAULT_ACCENT = 0xFFE6262B;

    /** Customizable from the GUI tab; not final, unlike the rest of the palette. */
    public static int ACCENT = DEFAULT_ACCENT;
    public static final int BACKGROUND = 0xC8141414;
    public static final int BACKGROUND_HOVER = 0xC81E1E1E;
    public static final int MODULE_BACKGROUND = 0xFF323232;
    public static final int OUTLINE = 0xFF0A0A0A;
    public static final int TEXT = 0xFFFFFFFF;
    public static final int TEXT_SECONDARY = 0xFFBBBBBB;
    public static final int TEXT_MUTED = 0xFF777777;
    public static final int SCROLLBAR = 0xC8282828;
}
