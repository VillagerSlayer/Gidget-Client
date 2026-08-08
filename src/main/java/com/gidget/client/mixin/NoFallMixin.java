package com.gidget.client.mixin;

import com.gidget.client.module.ModuleManager;
import com.gidget.client.module.impl.NoFallModule;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Fall damage is calculated server-side from the onGround transitions reported in the player's
 * position packets, not from anything the client can locally cancel. LocalPlayer#sendPosition
 * builds those packets from Entity#onGround() at five call sites (four packet variants plus the
 * lastOnGround tracking field); redirecting all of them to report "grounded" — but only inside this
 * method, leaving the real physics-facing onGround() alone everywhere else — tells the server we
 * never left the ground, without touching client-side jump/collision/animation logic.
 */
@Mixin(LocalPlayer.class)
public abstract class NoFallMixin {
    @Redirect(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;onGround()Z"))
    private boolean gidget$spoofOnGround(LocalPlayer self) {
        NoFallModule noFall = ModuleManager.get().get(NoFallModule.class);
        return noFall.isActive() || self.onGround();
    }
}
