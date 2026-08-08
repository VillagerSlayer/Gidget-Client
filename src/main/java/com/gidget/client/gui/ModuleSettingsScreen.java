package com.gidget.client.gui;

import com.gidget.client.module.Module;
import com.gidget.client.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Opened by right-clicking a module in {@link ClickGuiScreen}, matching Meteor's per-module settings window. */
public final class ModuleSettingsScreen extends Screen {
    private static final int PANEL_X = 12;
    private static final int PANEL_Y = 12;
    private static final int PANEL_WIDTH = 280;
    private static final int ROW_HEIGHT = 20;
    private static final int HEADER_HEIGHT = 40;

    private record ClickAction(int x0, int y0, int x1, int y1, Runnable action) {
        boolean contains(double x, double y) {
            return x >= x0 && y >= y0 && x < x1 && y < y1;
        }
    }

    private final Minecraft mc = Minecraft.getInstance();
    private final Screen parent;
    private final Module module;
    private final List<ClickAction> hitRegions = new ArrayList<>();

    private boolean bindingKey;
    private ColorSetting editingColor;
    private StringSetting editingString;
    private EditBox stringEditBox;
    private double lastClickX;
    private double lastClickY;
    private int scroll;

    public ModuleSettingsScreen(Screen parent, Module module) {
        super(Component.literal(module.getName()));
        this.parent = parent;
        this.module = module;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        hitRegions.clear();

        Font font = mc.font;
        int panelHeight = height - PANEL_Y * 2;

        graphics.fill(PANEL_X, PANEL_Y, PANEL_X + PANEL_WIDTH, PANEL_Y + panelHeight, GidgetTheme.BACKGROUND);
        graphics.fill(PANEL_X, PANEL_Y, PANEL_X + PANEL_WIDTH, PANEL_Y + HEADER_HEIGHT, GidgetTheme.MODULE_BACKGROUND);

        graphics.text(font, module.getName(), PANEL_X + 6, PANEL_Y + 6, GidgetTheme.TEXT);
        graphics.text(font, module.getDescription(), PANEL_X + 6, PANEL_Y + 18, GidgetTheme.TEXT_SECONDARY);

        String keyLabel = bindingKey ? "Press a key..." : "Bind: " + keyName(module.getKeyCode());
        int keyBoxX = PANEL_X + PANEL_WIDTH - 90;
        graphics.fill(keyBoxX, PANEL_Y + 6, PANEL_X + PANEL_WIDTH - 6, PANEL_Y + 6 + 14, GidgetTheme.BACKGROUND);
        graphics.text(font, keyLabel, keyBoxX + 4, PANEL_Y + 10, GidgetTheme.TEXT_SECONDARY);
        register(keyBoxX, PANEL_Y + 6, PANEL_X + PANEL_WIDTH - 6, PANEL_Y + 20, () -> bindingKey = true);

        int listTop = PANEL_Y + HEADER_HEIGHT + 4;
        int listBottom = PANEL_Y + panelHeight;
        graphics.enableScissor(PANEL_X, listTop, PANEL_X + PANEL_WIDTH, listBottom);

        int y = listTop - scroll;
        for (Setting<?> setting : module.getSettings().all()) {
            y = drawSetting(graphics, font, setting, PANEL_X + 4, y, PANEL_WIDTH - 8, listTop, listBottom);
        }

        graphics.disableScissor();

        drawColorPickerOverlay(graphics, font);
        drawStringEditorOverlay(graphics, font);

        graphics.text(font, "Right-click a module to get here. Esc to close.", PANEL_X + 6, height - 20, GidgetTheme.TEXT_SECONDARY);
    }

    private void openStringEditor(StringSetting setting) {
        editingString = setting;
        stringEditBox = new EditBox(mc.font, width / 2 - 90, height / 2 - 9, 180, 18, Component.literal(setting.getName()));
        stringEditBox.setValue(setting.get());
        addRenderableWidget(stringEditBox);
        setInitialFocus(stringEditBox);
    }

    private void closeStringEditor(boolean commit) {
        if (editingString == null) return;

        if (commit) editingString.set(stringEditBox.getValue());
        removeWidget(stringEditBox);
        editingString = null;
        stringEditBox = null;
    }

    private void drawStringEditorOverlay(GuiGraphicsExtractor graphics, Font font) {
        if (editingString == null) return;

        int px = width / 2 - 100;
        int py = height / 2 - 26;
        int pw = 200;
        int ph = 52;

        graphics.fill(px, py, px + pw, py + ph, 0xF0101010);
        graphics.text(font, "Editing: " + editingString.getName(), px + 6, py + 4, GidgetTheme.TEXT);

        int doneY = py + ph - 18;
        graphics.fill(px + 6, doneY, px + pw - 6, doneY + 14, GidgetTheme.ACCENT);
        graphics.centeredText(font, "Done", px + pw / 2, doneY + 3, GidgetTheme.TEXT);
        register(px + 6, doneY, px + pw - 6, doneY + 14, () -> closeStringEditor(true));
    }

    private int drawSetting(GuiGraphicsExtractor graphics, Font font, Setting<?> setting, int x, int y, int width, int top, int bottom) {
        int rowBottom = y + ROW_HEIGHT;
        boolean visible = rowBottom >= top && y <= bottom;

        if (visible) {
            graphics.fill(x, y, x + width, rowBottom - 1, GidgetTheme.BACKGROUND_HOVER);
            graphics.text(font, setting.getName(), x + 4, y + 6, GidgetTheme.TEXT_SECONDARY);

            int valueX = x + width - 90;

            if (setting instanceof BoolSetting bool) {
                boolean value = bool.get();
                graphics.fill(valueX, y + 4, valueX + 12, rowBottom - 5, value ? GidgetTheme.ACCENT : GidgetTheme.MODULE_BACKGROUND);
                register(valueX, y + 4, valueX + 12, rowBottom - 5, () -> bool.set(!bool.get()));
            } else if (setting instanceof IntSetting intS) {
                drawSlider(graphics, font, valueX, y, 86, (intS.get() - intS.getMin()) / (double) (intS.getMax() - intS.getMin()),
                    String.valueOf(intS.get()), frac -> intS.set((int) Math.round(intS.getMin() + frac * (intS.getMax() - intS.getMin()))));
            } else if (setting instanceof DoubleSetting doubleS) {
                drawSlider(graphics, font, valueX, y, 86, (doubleS.get() - doubleS.getMin()) / (doubleS.getMax() - doubleS.getMin()),
                    String.format("%.2f", doubleS.get()), frac -> doubleS.set(round2(doubleS.getMin() + frac * (doubleS.getMax() - doubleS.getMin()))));
            } else if (setting instanceof EnumSetting<?> enumS) {
                graphics.text(font, enumS.get().toString(), valueX, y + 6, GidgetTheme.TEXT);
                register(valueX, y, x + width, rowBottom, enumS::cycle);
            } else if (setting instanceof ColorSetting colorS) {
                GidgetColor c = colorS.get();
                graphics.fill(valueX, y + 4, valueX + 20, rowBottom - 5, c.toArgb() | 0xFF000000);
                register(valueX, y + 4, valueX + 20, rowBottom - 5, () -> editingColor = colorS);
            } else if (setting instanceof BlockListSetting blockList) {
                graphics.text(font, blockList.get().size() + " selected", valueX, y + 6, GidgetTheme.ACCENT);
                register(valueX, y, x + width, rowBottom, () -> mc.setScreen(new BlockListScreen(this, blockList)));
            } else if (setting instanceof EntityTypeListSetting entityList) {
                graphics.text(font, entityList.get().size() + " selected", valueX, y + 6, GidgetTheme.ACCENT);
                register(valueX, y, x + width, rowBottom, () -> mc.setScreen(new EntityTypeListScreen(this, entityList)));
            } else if (setting instanceof ItemListSetting itemList) {
                graphics.text(font, itemList.get().size() + " selected", valueX, y + 6, GidgetTheme.ACCENT);
                register(valueX, y, x + width, rowBottom, () -> mc.setScreen(new ItemListScreen(this, itemList)));
            } else if (setting instanceof StringSetting stringS) {
                String preview = stringS.get().isEmpty() ? "(empty)" : stringS.get();
                graphics.text(font, preview, valueX, y + 6, GidgetTheme.ACCENT);
                register(valueX, y, x + width, rowBottom, () -> openStringEditor(stringS));
            }
        }

        return rowBottom;
    }

    private void drawSlider(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, double fraction, String label, java.util.function.DoubleConsumer onChange) {
        int barY = y + 8;
        graphics.fill(x, barY, x + width, barY + 3, GidgetTheme.MODULE_BACKGROUND);
        int filled = (int) (width * Math.max(0, Math.min(1, fraction)));
        graphics.fill(x, barY, x + filled, barY + 3, GidgetTheme.ACCENT);
        graphics.text(font, label, x, y - 8, GidgetTheme.TEXT);

        register(x, y, x + width, y + ROW_HEIGHT, () -> onChange.accept(clamp01((lastClickX - x) / (double) width)));
    }

    private void drawColorPickerOverlay(GuiGraphicsExtractor graphics, Font font) {
        if (editingColor == null) return;

        int px = width / 2 - 90;
        int py = height / 2 - 60;
        int pw = 180;
        int ph = 120;

        graphics.fill(px, py, px + pw, py + ph, 0xF0101010);
        graphics.text(font, "Editing: " + editingColor.getName(), px + 6, py + 6, GidgetTheme.TEXT);

        GidgetColor color = editingColor.get();
        float[] hsv = color.toHsv();

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
            editingColor.set(GidgetColor.fromHsv(hsv[0], (float) s, (float) v, color.a));
        });

        int hueX = svX + svSize + 8;
        int hueY = svY;
        int hueHeight = svSize;
        for (int j = 0; j < hueHeight; j++) {
            float hue = j / (float) hueHeight;
            GidgetColor sample = GidgetColor.fromHsv(hue, 1.0F, 1.0F, 255);
            graphics.fill(hueX, hueY + j, hueX + 12, hueY + j + 1, sample.toArgb() | 0xFF000000);
        }
        register(hueX, hueY, hueX + 12, hueY + hueHeight, () -> {
            float hue = (float) clamp01((lastClickY - hueY) / (double) hueHeight);
            float[] current = editingColor.get().toHsv();
            editingColor.set(GidgetColor.fromHsv(hue, current[1], current[2], editingColor.get().a));
        });

        int alphaY = svY + svSize + 10;
        graphics.fill(svX, alphaY, svX + svSize, alphaY + 8, GidgetTheme.MODULE_BACKGROUND);
        int alphaFilled = (int) (svSize * (color.a / 255.0));
        graphics.fill(svX, alphaY, svX + alphaFilled, alphaY + 8, GidgetTheme.TEXT_SECONDARY);
        graphics.text(font, "alpha", svX, alphaY + 10, GidgetTheme.TEXT_SECONDARY);
        register(svX, alphaY, svX + svSize, alphaY + 8, () -> {
            int a = (int) Math.round(clamp01((lastClickX - svX) / (double) svSize) * 255);
            GidgetColor current = editingColor.get();
            editingColor.set(new GidgetColor(current.r, current.g, current.b, a));
        });

        register(px + pw - 16, py + 2, px + pw - 2, py + 14, () -> editingColor = null);
        graphics.text(font, "X", px + pw - 12, py + 5, 0xFFFF6666);
    }

    private void register(int x0, int y0, int x1, int y1, Runnable action) {
        hitRegions.add(new ClickAction(x0, y0, x1, y1, action));
    }

    private static double clamp01(double v) {
        return Math.max(0, Math.min(1, v));
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static String keyName(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) return "NONE";
        String name = GLFW.glfwGetKeyName(keyCode, 0);
        return name != null ? name.toUpperCase() : "KEY_" + keyCode;
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

        if (editingColor != null) {
            editingColor = null;
            return true;
        }

        if (editingString != null) {
            closeStringEditor(true);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (editingColor != null) {
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(0, scroll - (int) (scrollY * ROW_HEIGHT));
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (bindingKey) {
            if (event.key() == GLFW.GLFW_KEY_ESCAPE || event.key() == GLFW.GLFW_KEY_BACKSPACE) {
                module.setKeyCode(GLFW.GLFW_KEY_UNKNOWN);
            } else {
                module.setKeyCode(event.key());
            }
            bindingKey = false;
            return true;
        }

        if (editingString != null && event.key() == GLFW.GLFW_KEY_ESCAPE) {
            closeStringEditor(false);
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !bindingKey && editingColor == null && editingString == null;
    }

    @Override
    public void onClose() {
        mc.setScreen(parent);
    }
}
