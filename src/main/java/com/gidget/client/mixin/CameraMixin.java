package com.gidget.client.mixin;

import com.gidget.client.module.ModuleManager;
import com.gidget.client.module.impl.FreecamModule;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ported from Meteor Client's CameraMixin: overrides the position/rotation Camera#alignWithEntity
 * computes from the player entity, substituting FreecamModule's own decoupled coordinates instead.
 * This is what lets the camera roam freely while the actual player entity never moves.
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private boolean detached;

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void gidget$markDetached(float partialTicks, CallbackInfo ci) {
        if (ModuleManager.get().get(FreecamModule.class).isActive()) {
            this.detached = true;
        }
    }

    @Redirect(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"))
    private void gidget$redirectSetPosition(Camera instance, double x, double y, double z, float partialTicks) {
        FreecamModule freecam = ModuleManager.get().get(FreecamModule.class);
        if (freecam.isActive()) {
            this.setPosition(freecam.getX(partialTicks), freecam.getY(partialTicks), freecam.getZ(partialTicks));
        } else {
            this.setPosition(x, y, z);
        }
    }

    @Redirect(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V"))
    private void gidget$redirectSetRotation(Camera instance, float yRot, float xRot, float partialTicks) {
        FreecamModule freecam = ModuleManager.get().get(FreecamModule.class);
        if (freecam.isActive()) {
            this.setRotation(freecam.getYaw(partialTicks), freecam.getPitch(partialTicks));
        } else {
            this.setRotation(yRot, xRot);
        }
    }
}
