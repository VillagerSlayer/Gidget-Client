package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Hops just before attacking so the hit lands as a critical (vanilla requires falling + not on ground/ladder/water). */
public final class CriticalsModule extends Module {
    private static final double JUMP_VELOCITY = 0.42;

    public CriticalsModule() {
        super(Category.COMBAT, "criticals", "Ensures your hits land as critical strikes.");
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!mc.options.keyAttack.isDown()) return;
        if (!mc.player.onGround()) return;
        if (!(mc.hitResult instanceof EntityHitResult) || mc.hitResult.getType() != HitResult.Type.ENTITY) return;

        Vec3 motion = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(motion.x, JUMP_VELOCITY, motion.z);
    }
}
