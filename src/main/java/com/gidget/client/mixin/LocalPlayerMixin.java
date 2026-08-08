package com.gidget.client.mixin;

import com.gidget.client.module.ModuleManager;
import com.gidget.client.module.impl.NoSlowModule;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * LocalPlayer#modifyInput scales the movement input vector by the held item's use-speed multiplier
 * whenever isUsingItem() is true (eating, blocking, drawing a bow) — that's the actual slowdown
 * mechanism in this version, not a flat speed attribute. It calls Vec2#scale three times: the
 * general 0.98F scale (ordinal 0), the item-use scale (ordinal 1, the one we want to skip), and the
 * sneaking scale (ordinal 2). Redirecting only the second occurrence leaves sneaking behavior intact.
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Redirect(
        method = "modifyInput",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec2;scale(F)Lnet/minecraft/world/phys/Vec2;", ordinal = 1)
    )
    private Vec2 gidget$noSlowSkipItemUseScale(Vec2 instance, float factor) {
        NoSlowModule noSlow = ModuleManager.get().get(NoSlowModule.class);
        return noSlow.isActive() ? instance : instance.scale(factor);
    }
}
