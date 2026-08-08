package com.gidget.client.macro;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public final class MacroManager {
    private static final MacroManager INSTANCE = new MacroManager();

    private final List<Macro> macros = new ArrayList<>();

    private MacroManager() {
    }

    public static MacroManager get() {
        return INSTANCE;
    }

    public List<Macro> getMacros() {
        return macros;
    }

    public void add(Macro macro) {
        macros.add(macro);
    }

    public void remove(Macro macro) {
        macros.remove(macro);
    }

    public void onKeyPressed(int keyCode) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        for (Macro macro : macros) {
            if (macro.keyCode != keyCode) continue;

            if (macro.message.startsWith("/")) {
                mc.player.connection.sendCommand(macro.message.substring(1));
            } else {
                mc.player.connection.sendChat(macro.message);
            }
        }
    }
}
