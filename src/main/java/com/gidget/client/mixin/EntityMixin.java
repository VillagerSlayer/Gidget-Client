package com.gidget.client.mixin;

import com.gidget.client.module.ModuleManager;
import com.gidget.client.module.impl.FreecamModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ported from Meteor Client's EntityMixin: while Freecam is active, mouse-look deltas are
 * redirected into FreecamModule's own yaw/pitch instead of the real player's rotation (cancelling
 * Entity#turn entirely), matching the exact 0.15F sensitivity scale vanilla's turn() applies
 * internally. Without this, moving the mouse would keep turning the real player in place even
 * though the camera view itself is decoupled.
 */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void gidget$onTurn(double xo, double yo, CallbackInfo ci) {
        if ((Object) this != Minecraft.getInstance().player) return;

        FreecamModule freecam = ModuleManager.get().get(FreecamModule.class);
        if (freecam.isActive()) {
            freecam.changeLookDirection(xo * 0.15, yo * 0.15);
            ci.cancel();
        }
    }
}
