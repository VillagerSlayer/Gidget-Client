package com.gidget.client.mixin;

import com.gidget.client.module.ModuleManager;
import com.gidget.client.module.impl.FullbrightModule;
import com.gidget.client.module.impl.XrayModule;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ported from Meteor Client's real LightmapMixin. The shader-input approach (forcing
 * ambientColor/brightness on the LightmapRenderState) still runs the normal lightmap shader with
 * modified inputs and evidently doesn't reliably reach true white in practice. Meteor instead
 * cancels the shader-based update entirely and directly clears the lightmap texture to solid
 * opaque white on the GPU, which is unambiguous regardless of what the shader math would have
 * otherwise produced.
 */
@Mixin(Lightmap.class)
public abstract class LightmapMixin {
    @Shadow
    @Final
    private GpuTexture texture;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void gidget$fullbright(LightmapRenderState renderState, CallbackInfo ci) {
        FullbrightModule fullbright = ModuleManager.get().get(FullbrightModule.class);
        XrayModule xray = ModuleManager.get().get(XrayModule.class);

        if (fullbright.isActive() || (xray.isActive() && xray.isFullbright())) {
            ProfilerFiller profiler = Profiler.get();
            profiler.push("lightmap");
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(this.texture, -1);
            profiler.pop();
            ci.cancel();
        }
    }
}
