package com.gidget.client.mixin;

import com.gidget.client.module.ModuleManager;
import com.gidget.client.module.impl.XrayModule;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.FluidRenderer.Output;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {
    // shouldRenderFace only governs face culling between adjacent solid/fluid blocks, not whether
    // the fluid's outward surface (against air) draws at all — cancelling tesselate entirely is
    // what actually makes water/lava invisible, matching Meteor's real Xray implementation.
    @Inject(method = "tesselate", at = @At("HEAD"), cancellable = true)
    private void gidget$xrayHideFluid(BlockAndTintGetter level, BlockPos pos, Output output, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        XrayModule xray = ModuleManager.get().get(XrayModule.class);
        if (xray.isActive() && xray.isSeeThroughFluids()) {
            ci.cancel();
        }
    }
}
