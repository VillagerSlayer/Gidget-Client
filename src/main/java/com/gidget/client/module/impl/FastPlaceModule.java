package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Normal input handling only attempts one placement per initial click. This repeats the attempt
 * every tick the use key is held, instead of waiting for a fresh click.
 */
public final class FastPlaceModule extends Module {
    public FastPlaceModule() {
        super(Category.WORLD, "fast-place", "Allows rapid block placement while holding the use key.");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) return;
        if (!mc.options.keyUse.isDown()) return;
        if (!(mc.hitResult instanceof BlockHitResult blockHit) || mc.hitResult.getType() != HitResult.Type.BLOCK) return;

        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, blockHit);
    }
}
