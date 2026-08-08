package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Stops the player at a ledge instead of walking off it, by checking one step ahead each tick and
 * cancelling horizontal motion if it would leave solid ground with nothing below.
 */
public final class SafeWalkModule extends Module {
    public SafeWalkModule() {
        super(Category.MOVEMENT, "safe-walk", "Prevents walking or falling off block edges.");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || !mc.player.onGround()) return;

        Vec3 motion = mc.player.getDeltaMovement();
        if (motion.x == 0 && motion.z == 0) return;

        Vec3 next = mc.player.position().add(motion.x, 0, motion.z);
        BlockPos below = BlockPos.containing(next.x, mc.player.getY() - 0.1, next.z);

        if (mc.level.getBlockState(below).isAir()) {
            mc.player.setDeltaMovement(0, motion.y, 0);
        }
    }
}
