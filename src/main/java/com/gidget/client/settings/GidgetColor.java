package com.gidget.client.settings;

/** Simple RGBA color with HSV conversion helpers, used by {@link ColorSetting} and the color picker widget. */
public final class GidgetColor {
    public int r, g, b, a;

    public GidgetColor(int r, int g, int b, int a) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
        this.a = clamp(a);
    }

    public GidgetColor(int r, int g, int b) {
        this(r, g, b, 255);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    public int toArgb() {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public int toRgb() {
        return (r << 16) | (g << 8) | b;
    }

    public float[] toHsv() {
        return java.awt.Color.RGBtoHSB(r, g, b, null);
    }

    public static GidgetColor fromHsv(float h, float s, float v, int a) {
        int rgb = java.awt.Color.HSBtoRGB(h, s, v);
        return new GidgetColor((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, a);
    }

    public GidgetColor copy() {
        return new GidgetColor(r, g, b, a);
    }
}
