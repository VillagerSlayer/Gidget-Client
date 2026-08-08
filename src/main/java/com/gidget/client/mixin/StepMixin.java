package com.gidget.client.mixin;

import com.gidget.client.module.ModuleManager;
import com.gidget.client.module.impl.StepModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class StepMixin {
    @Inject(method = "maxUpStep", at = @At("RETURN"), cancellable = true)
    private void gidget$step(CallbackInfoReturnable<Float> cir) {
        if ((Object) this != Minecraft.getInstance().player) return;

        StepModule step = ModuleManager.get().get(StepModule.class);
        if (step.isActive() && cir.getReturnValue() < 1.0F) {
            cir.setReturnValue(1.0F);
        }
    }
}
