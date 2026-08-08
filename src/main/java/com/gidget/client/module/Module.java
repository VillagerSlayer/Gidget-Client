package com.gidget.client.module;

import com.gidget.client.settings.SettingGroup;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public abstract class Module {
    protected final Minecraft mc = Minecraft.getInstance();

    private final String name;
    private final String description;
    private final Category category;
    private final SettingGroup settings = new SettingGroup();

    private boolean active;
    private int keyCode = GLFW.GLFW_KEY_UNKNOWN;

    protected Module(Category category, String name, String description) {
        this.category = category;
        this.name = name;
        this.description = description;
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }

    public final Category getCategory() {
        return category;
    }

    public final SettingGroup getSettings() {
        return settings;
    }

    public final boolean isActive() {
        return active;
    }

    public final int getKeyCode() {
        return keyCode;
    }

    public final void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public final void toggle() {
        setActive(!active);
    }

    public final void setActive(boolean active) {
        if (this.active == active) return;
        this.active = active;

        if (active) onActivate();
        else onDeactivate();
    }

    /** Called every client tick while active. */
    public void onTick() {
    }

    /** Called when the module becomes active. */
    protected void onActivate() {
    }

    /** Called when the module becomes inactive. */
    protected void onDeactivate() {
    }

    public boolean matchesKey(int keyCode) {
        return this.keyCode != GLFW.GLFW_KEY_UNKNOWN && this.keyCode == keyCode;
    }
}
