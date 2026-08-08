package com.gidget.client.gui;

import com.gidget.client.config.ClientSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** The "HUD" tab: toggles for in-game overlay elements. Currently just the active-modules list. */
public final class HudSettingsScreen extends Screen {
    private final Minecraft mc = Minecraft.getInstance();
    private final Screen parent;

    private record ClickAction(int x0, int y0, int x1, int y1, Runnable action) {
        boolean contains(double x, double y) {
            return x >= x0 && y >= y0 && x < x1 && y < y1;
        }
    }

    private final List<ClickAction> hitRegions = new ArrayList<>();

    public HudSettingsScreen(Screen parent) {
        super(Component.literal("HUD Settings"));
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
        ClientSettings settings = ClientSettings.get();

        rowY = drawToggle(graphics, font, rowX, rowY, "Show active module list", settings.hudShowModules, v -> settings.hudShowModules = v);
        rowY = drawToggle(graphics, font, rowX, rowY, "Show FPS", settings.hudShowFps, v -> settings.hudShowFps = v);
        rowY = drawToggle(graphics, font, rowX, rowY, "Show ping", settings.hudShowPing, v -> settings.hudShowPing = v);
        drawToggle(graphics, font, rowX, rowY, "Show coordinates", settings.hudShowCoords, v -> settings.hudShowCoords = v);

        graphics.centeredText(font, "Esc to close.", width / 2, height - 20, GidgetTheme.TEXT_MUTED);
    }

    private int drawToggle(GuiGraphicsExtractor graphics, Font font, int rowX, int rowY, String label, boolean value, java.util.function.Consumer<Boolean> setter) {
        graphics.text(font, label, rowX, rowY + 4, GidgetTheme.TEXT_SECONDARY);

        int color = value ? GidgetTheme.ACCENT : GidgetTheme.MODULE_BACKGROUND;
        graphics.fill(rowX + 160, rowY, rowX + 172, rowY + 12, color);
        register(rowX + 160, rowY, rowX + 172, rowY + 12, () -> setter.accept(!value));

        return rowY + 18;
    }

    private void register(int x0, int y0, int x1, int y1, Runnable action) {
        hitRegions.add(new ClickAction(x0, y0, x1, y1, action));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;

        for (ClickAction action : hitRegions) {
            if (action.contains(event.x(), event.y())) {
                action.action().run();
                return true;
            }
        }

        return false;
    }

    @Override
    public void onClose() {
        mc.setScreen(parent);
    }
}
