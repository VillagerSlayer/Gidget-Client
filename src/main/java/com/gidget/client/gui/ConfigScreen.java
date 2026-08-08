package com.gidget.client.gui;

import com.gidget.client.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** The "Config" tab: named config profiles (save current state, load, delete). */
public final class ConfigScreen extends Screen {
    private static final int ROW_HEIGHT = 16;

    private final Minecraft mc = Minecraft.getInstance();
    private final Screen parent;

    private record ClickAction(int x0, int y0, int x1, int y1, Runnable action) {
        boolean contains(double x, double y) {
            return x >= x0 && y >= y0 && x < x1 && y < y1;
        }
    }

    private final List<ClickAction> hitRegions = new ArrayList<>();
    private EditBox nameBox;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Config Profiles"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        nameBox = new EditBox(mc.font, width / 2 - 100, 26, 160, 18, Component.literal("Profile name"));
        addRenderableWidget(nameBox);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xA0000000);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        hitRegions.clear();

        Font font = mc.font;
        graphics.centeredText(font, getTitle(), width / 2, 6, GidgetTheme.TEXT);

        int saveX = width / 2 + 64;
        graphics.fill(saveX, 26, saveX + 60, 44, GidgetTheme.MODULE_BACKGROUND);
        graphics.centeredText(font, "Save As", saveX + 30, 32, GidgetTheme.TEXT);
        register(saveX, 26, saveX + 60, 44, () -> {
            String name = nameBox.getValue().trim();
            if (!name.isEmpty()) ConfigManager.saveProfile(name);
        });

        int listX = width / 2 - 150;
        int listWidth = 300;
        int top = 56;
        int bottom = height - 30;

        graphics.enableScissor(listX, top, listX + listWidth, bottom);

        int y = top;
        for (String profile : ConfigManager.listProfiles()) {
            graphics.fill(listX, y, listX + listWidth, y + ROW_HEIGHT - 1, GidgetTheme.BACKGROUND);
            graphics.text(font, profile, listX + 4, y + 4, GidgetTheme.TEXT);

            int deleteX = listX + listWidth - 40;
            graphics.text(font, "Delete", deleteX, y + 4, 0xFFFF6666);

            int rowX0 = listX;
            int rowY0 = y;
            int rowY1 = y + ROW_HEIGHT - 1;
            register(rowX0, rowY0, deleteX - 4, rowY1, () -> ConfigManager.loadProfile(profile));
            register(deleteX - 4, rowY0, listX + listWidth, rowY1, () -> ConfigManager.deleteProfile(profile));

            y += ROW_HEIGHT;
        }

        graphics.disableScissor();

        graphics.centeredText(font, "Click a profile to load it, Delete to remove it. Esc to close.", width / 2, height - 20, GidgetTheme.TEXT_MUTED);
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
