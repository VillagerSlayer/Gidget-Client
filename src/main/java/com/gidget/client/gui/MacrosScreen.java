package com.gidget.client.gui;

import com.gidget.client.config.ConfigManager;
import com.gidget.client.macro.Macro;
import com.gidget.client.macro.MacroManager;
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

/** The "Macros" tab: a named keybind -> chat message (or, prefixed with '/', command). Saved immediately on add/remove. */
public final class MacrosScreen extends Screen {
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
    private EditBox messageBox;
    private boolean bindingKey;
    private int pendingKey = GLFW.GLFW_KEY_UNKNOWN;

    public MacrosScreen(Screen parent) {
        super(Component.literal("Macros"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        nameBox = new EditBox(mc.font, width / 2 - 150, 34, 130, 18, Component.literal("Name"));
        addRenderableWidget(nameBox);

        messageBox = new EditBox(mc.font, width / 2 - 12, 34, 162, 18, Component.literal("Message or /command"));
        addRenderableWidget(messageBox);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xA0000000);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        hitRegions.clear();

        Font font = mc.font;
        graphics.centeredText(font, getTitle(), width / 2, 6, GidgetTheme.TEXT);

        graphics.text(font, "Name", width / 2 - 150, 24, GidgetTheme.TEXT_SECONDARY);
        graphics.text(font, "Message or /command", width / 2 - 12, 24, GidgetTheme.TEXT_SECONDARY);

        int bindX = width / 2 - 150;
        int bindY = 58;
        graphics.fill(bindX, bindY, bindX + 70, bindY + 18, GidgetTheme.MODULE_BACKGROUND);
        graphics.centeredText(font, bindingKey ? "Press a key..." : keyName(pendingKey), bindX + 35, bindY + 5, GidgetTheme.TEXT);
        register(bindX, bindY, bindX + 70, bindY + 18, () -> bindingKey = true);

        int addX = bindX + 78;
        boolean canAdd = !nameBox.getValue().isBlank() && !messageBox.getValue().isBlank() && pendingKey != GLFW.GLFW_KEY_UNKNOWN;
        graphics.fill(addX, bindY, addX + 60, bindY + 18, canAdd ? GidgetTheme.ACCENT : GidgetTheme.MODULE_BACKGROUND);
        graphics.centeredText(font, "Add", addX + 30, bindY + 5, GidgetTheme.TEXT);
        register(addX, bindY, addX + 60, bindY + 18, () -> {
            if (!canAdd) return;

            MacroManager.get().add(new Macro(nameBox.getValue().trim(), pendingKey, messageBox.getValue().trim()));
            ConfigManager.save();

            pendingKey = GLFW.GLFW_KEY_UNKNOWN;
            nameBox.setValue("");
            messageBox.setValue("");
        });

        int listX = width / 2 - 150;
        int listWidth = 300;
        int top = 84;
        int bottom = height - 30;

        graphics.enableScissor(listX, top, listX + listWidth, bottom);

        int y = top;
        for (Macro macro : new ArrayList<>(MacroManager.get().getMacros())) {
            graphics.fill(listX, y, listX + listWidth, y + ROW_HEIGHT - 1, GidgetTheme.BACKGROUND);
            graphics.text(font, macro.name + "  [" + keyName(macro.keyCode) + "]  " + macro.message, listX + 4, y + 4, GidgetTheme.TEXT);

            int removeX = listX + listWidth - 40;
            graphics.text(font, "Remove", removeX, y + 4, 0xFFFF6666);
            register(removeX - 4, y, listX + listWidth, y + ROW_HEIGHT - 1, () -> {
                MacroManager.get().remove(macro);
                ConfigManager.save();
            });

            y += ROW_HEIGHT;
        }

        graphics.disableScissor();

        graphics.centeredText(font, "Name it, bind a key, type a message, Add. Esc to close.", width / 2, height - 20, GidgetTheme.TEXT_MUTED);
    }

    private void register(int x0, int y0, int x1, int y1, Runnable action) {
        hitRegions.add(new ClickAction(x0, y0, x1, y1, action));
    }

    private static String keyName(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) return "NONE";
        String name = GLFW.glfwGetKeyName(keyCode, 0);
        return name != null ? name.toUpperCase() : "KEY_" + keyCode;
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
    public boolean keyPressed(KeyEvent event) {
        if (bindingKey) {
            pendingKey = event.key() == GLFW.GLFW_KEY_ESCAPE ? GLFW.GLFW_KEY_UNKNOWN : event.key();
            bindingKey = false;
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !bindingKey;
    }

    @Override
    public void onClose() {
        mc.setScreen(parent);
    }
}
