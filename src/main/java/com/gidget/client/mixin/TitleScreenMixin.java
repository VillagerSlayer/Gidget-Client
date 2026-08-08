package com.gidget.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Credit text in the top-right corner; the logo itself sits centered right under the vanilla
 * Minecraft logo, using LogoRenderer's own layout math (logo at y=30, height 44; edition badge
 * extends to y=30+44-7+14=81) rather than a guessed offset, so it lines up at any GUI scale.
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {
    private static final Identifier LOGO = Identifier.fromNamespaceAndPath("gidgetclient", "textures/gui/logo.png");
    private static final int LOGO_SOURCE_WIDTH = 1024;
    private static final int LOGO_SOURCE_HEIGHT = 559;
    private static final int LOGO_DRAW_WIDTH = 140;
    private static final int LOGO_DRAW_HEIGHT = (LOGO_DRAW_WIDTH * LOGO_SOURCE_HEIGHT) / LOGO_SOURCE_WIDTH;
    private static final int VANILLA_LOGO_BOTTOM = 81;

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void gidget$drawBranding(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        String prefix = "Gidget Client created by ";
        String name = "VillagerSlayer";
        int prefixWidth = mc.font.width(prefix);
        int nameWidth = mc.font.width(name);
        int textWidth = prefixWidth + nameWidth;

        int margin = 6;
        int textX = graphics.guiWidth() - textWidth - margin;
        int textY = margin;
        graphics.text(mc.font, prefix, textX, textY, 0xFFAAAAAA);
        graphics.text(mc.font, name, textX + prefixWidth, textY, 0xFFFF4444);

        int logoX = graphics.guiWidth() / 2 - LOGO_DRAW_WIDTH / 2;
        int logoY = VANILLA_LOGO_BOTTOM + 4;
        graphics.blit(LOGO, logoX, logoY, logoX + LOGO_DRAW_WIDTH, logoY + LOGO_DRAW_HEIGHT, 0.0F, 1.0F, 0.0F, 1.0F);
    }
}
