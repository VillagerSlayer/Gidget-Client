package com.gidget.client.mixin;

import com.gidget.client.module.ModuleManager;
import com.gidget.client.module.impl.XrayModule;
import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The lightmap texture (LightmapMixin) only controls what color the sampled light COORDINATE maps
 * to. Ambient occlusion and per-face diffuse shading live entirely separately, baked as a per-vertex
 * tint color on QuadInstance that gets multiplied against the lightmap sample — so a dark AO tint
 * still looks dark even against a pure-white lightmap texture. Forcing that tint back to -1 (opaque
 * white, QuadInstance's own "no tint" default) after both quad-preparation paths removes AO and
 * face shading entirely while active.
 *
 * Xray-only: the standalone Fullbright module intentionally does NOT trigger this, matching real
 * Meteor Client's Fullbright exactly (lightmap-clear only, AO/shading untouched).
 */
@Mixin(BlockModelLighter.class)
public abstract class BlockModelLighterMixin {
    @Inject(method = "prepareQuadAmbientOcclusion", at = @At("TAIL"))
    private void gidget$noAo(BlockAndTintGetter level, BlockState state, BlockPos pos, BakedQuad quad, QuadInstance quadInstance, CallbackInfo ci) {
        if (gidget$fullbrightActive()) {
            quadInstance.setColor(-1);
        }
    }

    @Inject(method = "prepareQuadFlat", at = @At("TAIL"))
    private void gidget$noFlatShade(BlockAndTintGetter level, BlockState state, BlockPos pos, int lightCoords, BakedQuad quad, QuadInstance quadInstance, CallbackInfo ci) {
        if (gidget$fullbrightActive()) {
            quadInstance.setColor(-1);
        }
    }

    private static boolean gidget$fullbrightActive() {
        XrayModule xray = ModuleManager.get().get(XrayModule.class);
        return xray.isActive() && xray.isFullbright();
    }
}
