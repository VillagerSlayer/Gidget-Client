package com.gidget.client.gui;

import com.gidget.client.settings.GidgetColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** The "GUI" tab: customizes the ClickGUI's own appearance. Currently just the accent color. */
public final class GuiSettingsScreen extends Screen {
    private final Minecraft mc = Minecraft.getInstance();
    private final Screen parent;

    private record ClickAction(int x0, int y0, int x1, int y1, Runnable action) {
        boolean contains(double x, double y) {
            return x >= x0 && y >= y0 && x < x1 && y < y1;
        }
    }

    private final List<ClickAction> hitRegions = new ArrayList<>();
    private boolean editingColor;
    private double lastClickX;
    private double lastClickY;

    public GuiSettingsScreen(Screen parent) {
        super(Component.literal("GUI Settings"));
        this.parent = parent;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xA0000000);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        hitRegions.clear();

        Font font = mc.font;
        graphics.centeredText(font, getTitle(), width / 2, 6, GidgetTheme.TEXT);

        int rowX = width / 2 - 100;
        int rowY = 40;
        graphics.text(font, "Accent Color", rowX, rowY + 4, GidgetTheme.TEXT_SECONDARY);
        graphics.fill(rowX + 160, rowY, rowX + 180, rowY + 14, GidgetTheme.ACCENT);
        register(rowX + 160, rowY, rowX + 180, rowY + 14, () -> editingColor = true);

        if (GidgetTheme.ACCENT != GidgetTheme.DEFAULT_ACCENT) {
            graphics.text(font, "Reset", rowX + 190, rowY + 4, GidgetTheme.TEXT_SECONDARY);
            register(rowX + 190, rowY, rowX + 220, rowY + 14, () -> GidgetTheme.ACCENT = GidgetTheme.DEFAULT_ACCENT);
        }

        if (editingColor) drawColorPicker(graphics, font);

        graphics.centeredText(font, "Esc to close.", width / 2, height - 20, GidgetTheme.TEXT_MUTED);
    }

    private void drawColorPicker(GuiGraphicsExtractor graphics, Font font) {
        int px = width / 2 - 90;
        int py = height / 2 - 60;
        int pw = 180;

        GidgetColor color = new GidgetColor((GidgetTheme.ACCENT >> 16) & 0xFF, (GidgetTheme.ACCENT >> 8) & 0xFF, GidgetTheme.ACCENT & 0xFF, 255);
        float[] hsv = color.toHsv();

        graphics.fill(px, py, px + pw, py + 130, 0xF0101010);
        graphics.text(font, "Accent Color", px + 6, py + 6, GidgetTheme.TEXT);

        int svX = px + 6;
        int svY = py + 20;
        int svSize = 100;
        for (int i = 0; i < svSize; i += 4) {
            for (int j = 0; j < svSize; j += 4) {
                float s = i / (float) svSize;
                float v = 1.0F - j / (float) svSize;
                GidgetColor sample = GidgetColor.fromHsv(hsv[0], s, v, 255);
                graphics.fill(svX + i, svY + j, svX + i + 4, svY + j + 4, sample.toArgb() | 0xFF000000);
            }
        }
        register(svX, svY, svX + svSize, svY + svSize, () -> {
            double s = clamp01((lastClickX - svX) / (double) svSize);
            double v = 1.0 - clamp01((lastClickY - svY) / (double) svSize);
            GidgetTheme.ACCENT = (GidgetColor.fromHsv(hsv[0], (float) s, (float) v, 255).toArgb() | 0xFF000000);
        });

        int hueX = svX + svSize + 8;
        int hueY = svY;
        for (int j = 0; j < svSize; j++) {
            float hue = j / (float) svSize;
            GidgetColor sample = GidgetColor.fromHsv(hue, 1.0F, 1.0F, 255);
            graphics.fill(hueX, hueY + j, hueX + 12, hueY + j + 1, sample.toArgb() | 0xFF000000);
        }
        register(hueX, hueY, hueX + 12, hueY + svSize, () -> {
            float hue = (float) clamp01((lastClickY - hueY) / (double) svSize);
            GidgetTheme.ACCENT = (GidgetColor.fromHsv(hue, hsv[1], hsv[2], 255).toArgb() | 0xFF000000);
        });

        register(px + pw - 16, py + 2, px + pw - 2, py + 14, () -> editingColor = false);
        graphics.text(font, "X", px + pw - 12, py + 5, 0xFFFF6666);
    }

    private void register(int x0, int y0, int x1, int y1, Runnable action) {
        hitRegions.add(new ClickAction(x0, y0, x1, y1, action));
    }

    private static double clamp01(double v) {
        return Math.max(0, Math.min(1, v));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;

        lastClickX = event.x();
        lastClickY = event.y();

        for (ClickAction action : hitRegions) {
            if (action.contains(event.x(), event.y())) {
                action.action().run();
                return true;
            }
        }

        if (editingColor) {
            editingColor = false;
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (editingColor) {
            lastClickX = event.x();
            lastClickY = event.y();
            for (ClickAction action : hitRegions) {
                if (action.contains(event.x(), event.y())) {
                    action.action().run();
                    return true;
                }
            }
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !editingColor;
    }

    @Override
    public void onClose() {
        mc.setScreen(parent);
    }
}
