package com.gidget.client.gui;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Matches Meteor Client's real layout: every category is its own always-visible column with a
 * colored dropdown-style header, floating directly over the world with no background panel behind
 * the list. Left-click a module to toggle it, right-click to open its settings — Meteor's actual
 * interaction model, not an inline accordion.
 */
public final class ClickGuiScreen extends Screen {
    private static final int TOP_BAR_Y = 6;
    private static final int COLUMN_TOP = 24;
    private static final int COLUMN_WIDTH = 130;
    private static final int COLUMN_GAP = 6;
    private static final int HEADER_HEIGHT = 16;
    private static final int ROW_HEIGHT = 10;

    private record ClickAction(int x0, int y0, int x1, int y1, Runnable onLeftClick, Runnable onRightClick) {
        boolean contains(double x, double y) {
            return x >= x0 && y >= y0 && x < x1 && y < y1;
        }
    }

    private final Minecraft mc = Minecraft.getInstance();
    private final List<ClickAction> hitRegions = new ArrayList<>();
    private final Map<Category, Integer> scrollByCategory = new ConcurrentHashMap<>();
    private final Map<Category, int[]> columnBounds = new ConcurrentHashMap<>();

    public ClickGuiScreen() {
        super(Component.literal("Gidget Client"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        hitRegions.clear();

        Font font = mc.font;
        drawTopTabs(graphics, font);

        Category[] categories = Category.values();
        int totalWidth = categories.length * COLUMN_WIDTH + (categories.length - 1) * COLUMN_GAP;
        int startX = (width - totalWidth) / 2;

        for (int i = 0; i < categories.length; i++) {
            int x = startX + i * (COLUMN_WIDTH + COLUMN_GAP);
            drawColumn(graphics, font, categories[i], x, mouseX, mouseY);
        }

        graphics.centeredText(font, "Left click - Toggle module", width / 2, height - 20, GidgetTheme.TEXT_MUTED);
        graphics.centeredText(font, "Right click - Open module settings", width / 2, height - 10, GidgetTheme.TEXT_MUTED);
    }

    private record Tab(String name, Runnable onClick) {
    }

    private Tab[] tabs() {
        return new Tab[]{
            new Tab("Modules", null),
            new Tab("Config", () -> mc.setScreen(new ConfigScreen(this))),
            new Tab("GUI", () -> mc.setScreen(new GuiSettingsScreen(this))),
            new Tab("HUD", () -> mc.setScreen(new HudSettingsScreen(this))),
            new Tab("Macros", () -> mc.setScreen(new MacrosScreen(this)))
        };
    }

    private void drawTopTabs(GuiGraphicsExtractor graphics, Font font) {
        Tab[] tabs = tabs();
        int totalWidth = 0;
        int[] widths = new int[tabs.length];
        for (int i = 0; i < tabs.length; i++) {
            widths[i] = font.width(tabs[i].name()) + 16;
            totalWidth += widths[i];
        }

        int x = (width - totalWidth) / 2;

        graphics.fill(x - 4, TOP_BAR_Y - 3, x + totalWidth + 4, TOP_BAR_Y + 17, GidgetTheme.BACKGROUND);
        graphics.fill(x - 4, TOP_BAR_Y + 16, x + totalWidth + 4, TOP_BAR_Y + 17, GidgetTheme.OUTLINE);

        for (Tab tab : tabs) {
            int w = font.width(tab.name()) + 16;
            boolean active = tab.onClick() == null;
            graphics.fill(x, TOP_BAR_Y, x + w - 2, TOP_BAR_Y + 14, active ? GidgetTheme.ACCENT : (GidgetTheme.ACCENT & 0x00FFFFFF) | 0x60000000);
            if (active) {
                graphics.fill(x, TOP_BAR_Y + 12, x + w - 2, TOP_BAR_Y + 14, GidgetTheme.OUTLINE);
            }
            graphics.centeredText(font, tab.name(), x + w / 2 - 1, TOP_BAR_Y + 3, active ? GidgetTheme.TEXT : GidgetTheme.TEXT_SECONDARY);

            if (tab.onClick() != null) {
                int tabX0 = x;
                int tabX1 = x + w - 2;
                register(tabX0, TOP_BAR_Y, tabX1, TOP_BAR_Y + 14, tab.onClick(), null);
            }

            x += w;
        }
    }

    private void drawColumn(GuiGraphicsExtractor graphics, Font font, Category category, int x, int mouseX, int mouseY) {
        List<Module> modules = ModuleManager.get().getModules(category);

        int listTop = COLUMN_TOP + HEADER_HEIGHT + 2;
        int listBottom = height - 26;
        int visibleHeight = listBottom - listTop;
        int contentHeight = modules.size() * ROW_HEIGHT;
        int maxScroll = Math.max(0, contentHeight - visibleHeight);
        int scroll = Math.min(scrollByCategory.getOrDefault(category, 0), maxScroll);

        // Panel + border behind the list so text stays legible over the world, matching the header.
        graphics.fill(x, listTop, x + COLUMN_WIDTH, listBottom, GidgetTheme.BACKGROUND);
        graphics.fill(x, listTop, x + COLUMN_WIDTH, listTop + 1, GidgetTheme.OUTLINE);
        graphics.fill(x, listBottom - 1, x + COLUMN_WIDTH, listBottom, GidgetTheme.OUTLINE);
        graphics.fill(x, listTop, x + 1, listBottom, GidgetTheme.OUTLINE);
        graphics.fill(x + COLUMN_WIDTH - 1, listTop, x + COLUMN_WIDTH, listBottom, GidgetTheme.OUTLINE);

        graphics.fill(x, COLUMN_TOP, x + COLUMN_WIDTH, COLUMN_TOP + HEADER_HEIGHT, GidgetTheme.ACCENT);
        graphics.fill(x, COLUMN_TOP + HEADER_HEIGHT - 1, x + COLUMN_WIDTH, COLUMN_TOP + HEADER_HEIGHT, GidgetTheme.OUTLINE);
        graphics.text(font, "✓", x + 4, COLUMN_TOP + 4, GidgetTheme.TEXT);
        graphics.centeredText(font, capitalize(category.name()), x + COLUMN_WIDTH / 2 + 4, COLUMN_TOP + 4, GidgetTheme.TEXT);
        graphics.text(font, "⌄", x + COLUMN_WIDTH - 12, COLUMN_TOP + 4, GidgetTheme.TEXT);

        graphics.enableScissor(x, listTop, x + COLUMN_WIDTH, listBottom);

        int y = listTop - scroll;
        for (Module module : modules) {
            boolean hovered = mouseX >= x && mouseX < x + COLUMN_WIDTH && mouseY >= y && mouseY < y + ROW_HEIGHT;
            boolean active = module.isActive();

            if (y + ROW_HEIGHT >= listTop && y <= listBottom) {
                if (active) {
                    graphics.fill(x + 1, y, x + COLUMN_WIDTH - 1, y + ROW_HEIGHT, (GidgetTheme.ACCENT & 0x00FFFFFF) | 0x33000000);
                    graphics.fill(x + 1, y, x + 3, y + ROW_HEIGHT, GidgetTheme.ACCENT);
                } else if (hovered) {
                    graphics.fill(x + 1, y, x + COLUMN_WIDTH - 1, y + ROW_HEIGHT, GidgetTheme.BACKGROUND_HOVER);
                }

                int color = active || hovered ? GidgetTheme.TEXT : GidgetTheme.TEXT_SECONDARY;
                graphics.text(font, module.getName(), x + 5, y + 1, color);

                if (module.getKeyCode() != GLFW.GLFW_KEY_UNKNOWN) {
                    String key = keyName(module.getKeyCode());
                    int keyWidth = font.width(key);
                    graphics.text(font, key, x + COLUMN_WIDTH - keyWidth - 4, y + 1, GidgetTheme.TEXT_MUTED);
                }

                int rowY = y;
                register(x, rowY, x + COLUMN_WIDTH, rowY + ROW_HEIGHT,
                    module::toggle,
                    () -> mc.setScreen(new ModuleSettingsScreen(this, module))
                );
            }

            y += ROW_HEIGHT;
        }

        graphics.disableScissor();

        if (maxScroll > 0) {
            int barX = x + COLUMN_WIDTH - 3;
            graphics.fill(barX, listTop + 1, barX + 2, listBottom - 1, GidgetTheme.SCROLLBAR);
            int thumbHeight = Math.max(10, visibleHeight * visibleHeight / contentHeight);
            int thumbY = listTop + 1 + scroll * (visibleHeight - thumbHeight - 2) / maxScroll;
            graphics.fill(barX, thumbY, barX + 2, thumbY + thumbHeight, GidgetTheme.ACCENT);
        }

        columnBounds.put(category, new int[]{x, listTop, x + COLUMN_WIDTH, listBottom, maxScroll});
    }

    private static String capitalize(String s) {
        return s.charAt(0) + s.substring(1).toLowerCase();
    }

    private static String keyName(int keyCode) {
        String name = GLFW.glfwGetKeyName(keyCode, 0);
        return name != null ? name.toUpperCase() : "KEY_" + keyCode;
    }

    private void register(int x0, int y0, int x1, int y1, Runnable onLeftClick, Runnable onRightClick) {
        hitRegions.add(new ClickAction(x0, y0, x1, y1, onLeftClick, onRightClick));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;

        for (ClickAction action : hitRegions) {
            if (!action.contains(event.x(), event.y())) continue;
            if (action.onLeftClick() == null && action.onRightClick() == null) continue;

            if (event.button() == 1 && action.onRightClick() != null) {
                action.onRightClick().run();
            } else if (event.button() == 0 && action.onLeftClick() != null) {
                action.onLeftClick().run();
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (Map.Entry<Category, int[]> entry : columnBounds.entrySet()) {
            int[] b = entry.getValue();
            if (mouseX >= b[0] && mouseX < b[2] && mouseY >= b[1] && mouseY < b[3]) {
                int maxScroll = b[4];
                scrollByCategory.merge(entry.getKey(), -(int) (scrollY * ROW_HEIGHT), Integer::sum);
                scrollByCategory.computeIfPresent(entry.getKey(), (k, v) -> Math.max(0, Math.min(v, maxScroll)));
                return true;
            }
        }
        return true;
    }
}
