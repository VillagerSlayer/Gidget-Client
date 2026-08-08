package com.gidget.client.hud;

import com.gidget.client.config.ClientSettings;
import com.gidget.client.gui.GidgetTheme;
import com.gidget.client.module.Module;
import com.gidget.client.module.ModuleManager;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ModuleListHudElement implements HudElement {
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ClientSettings settings = ClientSettings.get();
        int y = 4;

        if (settings.hudShowFps) {
            y = drawLine(graphics, mc, "FPS: " + mc.getFps(), y);
        }

        if (settings.hudShowPing) {
            int ping = -1;
            var info = mc.player.connection.getPlayerInfo(mc.player.getUUID());
            if (info != null) ping = info.getLatency();
            y = drawLine(graphics, mc, "Ping: " + (ping >= 0 ? ping + "ms" : "N/A"), y);
        }

        if (settings.hudShowCoords) {
            String coords = String.format("%.1f, %.1f, %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
            y = drawLine(graphics, mc, coords, y);
        }

        if (!settings.hudShowModules) return;

        for (Module module : ModuleManager.get().getModules()) {
            if (!module.isActive()) continue;
            y = drawLine(graphics, mc, module.getName(), y, GidgetTheme.ACCENT);
        }
    }

    private int drawLine(GuiGraphicsExtractor graphics, Minecraft mc, String text, int y) {
        return drawLine(graphics, mc, text, y, GidgetTheme.TEXT);
    }

    private int drawLine(GuiGraphicsExtractor graphics, Minecraft mc, String text, int y, int color) {
        int width = mc.font.width(text);
        graphics.text(mc.font, text, graphics.guiWidth() - width - 4, y, color);
        return y + 10;
    }
}
