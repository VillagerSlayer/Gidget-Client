package com.gidget.client.mixin;

import com.gidget.client.module.ModuleManager;
import com.gidget.client.module.impl.FreecamModule;
import com.gidget.client.module.impl.FullbrightModule;
import com.gidget.client.module.impl.XrayModule;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapRenderStateExtractorMixin {
    @Inject(method = "extract", at = @At("TAIL"))
    private void gidget$fullbright(LightmapRenderState renderState, float partialTicks, CallbackInfo ci) {
        FullbrightModule fullbright = ModuleManager.get().get(FullbrightModule.class);
        XrayModule xray = ModuleManager.get().get(XrayModule.class);
        FreecamModule freecam = ModuleManager.get().get(FreecamModule.class);

        // Xray forces full brightness too by default, matching Meteor: otherwise blocks it exposes
        // by hiding their neighbors are still lit as if still buried, and look black.
        if (fullbright.isActive() || (xray.isActive() && xray.isFullbright()) || freecam.isActive()) {
            renderState.ambientColor = LightmapRenderStateExtractor.WHITE;
        }
    }
}
