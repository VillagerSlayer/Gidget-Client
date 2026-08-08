package com.gidget.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Shared checklist UI backing {@link BlockListScreen} and {@link EntityTypeListScreen}: alphabetical,
 * selected entries pinned to the top, and an optional per-row icon.
 */
abstract class AbstractSelectListScreen<T> extends Screen {
    private static final int ROW_HEIGHT = 18;
    private static final int ICON_SIZE = 16;

    private record ClickAction(int x0, int y0, int x1, int y1, Runnable action) {
        boolean contains(double x, double y) {
            return x >= x0 && y >= y0 && x < x1 && y < y1;
        }
    }

    protected final Minecraft mc = Minecraft.getInstance();

    private final Screen parent;
    private final List<ClickAction> hitRegions = new ArrayList<>();
    private EditBox searchBox;
    private String query = "";
    private int scroll;

    protected AbstractSelectListScreen(Screen parent, Component title) {
        super(title);
        this.parent = parent;
    }

    protected abstract List<T> allItems();

    protected abstract String idFor(T item);

    protected abstract List<T> currentSelection();

    protected abstract void setSelection(List<T> items);

    /** Optional icon drawn to the left of the label; return null (the default) to skip it. */
    protected @Nullable ItemStack iconFor(T item) {
        return null;
    }

    @Override
    protected void init() {
        searchBox = new EditBox(mc.font, width / 2 - 100, 24, 200, 18, Component.literal("Search"));
        searchBox.setResponder(s -> {
            query = s.toLowerCase();
            scroll = 0;
        });
        addRenderableWidget(searchBox);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // Background must be drawn before super's widget rendering (the search box), not after —
        // otherwise the near-opaque overlay paints over the box and it reads as washed-out/grayed.
        graphics.fill(0, 0, width, height, 0xA0000000);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        hitRegions.clear();

        Font font = mc.font;
        graphics.centeredText(font, getTitle(), width / 2, 6, GidgetTheme.TEXT);

        List<T> selection = currentSelection();
        int listX = width / 2 - 150;
        int listWidth = 300;
        int top = 50;
        int bottom = height - 30;

        List<T> displayed = new ArrayList<>();
        for (T item : allItems()) {
            if (query.isEmpty() || idFor(item).toLowerCase().contains(query)) displayed.add(item);
        }
        displayed.sort(Comparator.<T>comparingInt(item -> selection.contains(item) ? 0 : 1).thenComparing(this::idFor));

        graphics.enableScissor(listX, top, listX + listWidth, bottom);

        int y = top - scroll;
        for (T item : displayed) {
            if (y + ROW_HEIGHT >= top && y <= bottom) {
                boolean selected = selection.contains(item);
                graphics.fill(listX, y, listX + listWidth, y + ROW_HEIGHT - 1, selected ? GidgetTheme.MODULE_BACKGROUND : GidgetTheme.BACKGROUND);
                if (selected) graphics.fill(listX, y, listX + 2, y + ROW_HEIGHT - 1, GidgetTheme.ACCENT);

                int textX = listX + 4;
                ItemStack icon = iconFor(item);
                if (icon != null && !icon.isEmpty()) {
                    graphics.item(icon, listX + 4, y + (ROW_HEIGHT - 1 - ICON_SIZE) / 2);
                    textX += ICON_SIZE + 4;
                }

                graphics.text(font, idFor(item), textX, y + (ROW_HEIGHT - 1) / 2 - 4, selected ? GidgetTheme.TEXT : GidgetTheme.TEXT_SECONDARY);

                int x0 = listX;
                int y0 = y;
                int x1 = listX + listWidth;
                int y1 = y + ROW_HEIGHT - 1;
                hitRegions.add(new ClickAction(x0, y0, x1, y1, () -> {
                    List<T> updated = new ArrayList<>(currentSelection());
                    if (!updated.remove(item)) updated.add(item);
                    setSelection(updated);
                }));
            }

            y += ROW_HEIGHT;
        }

        graphics.disableScissor();

        graphics.centeredText(font, "Click to toggle. Selected blocks stay pinned to the top. Esc to close.", width / 2, height - 20, 0xFF888888);
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(0, scroll - (int) (scrollY * ROW_HEIGHT));
        return true;
    }

    @Override
    public void onClose() {
        mc.setScreen(parent);
    }
}
