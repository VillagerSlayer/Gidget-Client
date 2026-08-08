package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import net.minecraft.world.phys.Vec3;

/**
 * Keeps the player at the surface of water/lava instead of sinking into it, by clamping position
 * and vertical velocity each tick rather than overriding the underlying fluid-collision code.
 */
public final class JesusModule extends Module {
    public JesusModule() {
        super(Category.MOVEMENT, "jesus", "Walk on top of water and lava instead of sinking into it.");
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!mc.player.isInWater() && !mc.player.isInLava()) return;
        if (mc.player.getAbilities().flying) return;

        double surfaceY = Math.floor(mc.player.getY()) + 1.0;
        if (mc.player.getY() < surfaceY) {
            mc.player.setPos(mc.player.getX(), surfaceY, mc.player.getZ());
        }

        Vec3 motion = mc.player.getDeltaMovement();
        if (motion.y < 0) {
            mc.player.setDeltaMovement(motion.x, 0, motion.z);
        }
    }
}
