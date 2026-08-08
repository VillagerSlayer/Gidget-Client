package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.settings.DoubleSetting;
import net.minecraft.world.phys.Vec3;

public final class SpeedModule extends Module {
    public final DoubleSetting multiplier = getSettings().add(new DoubleSetting(
        "multiplier", "Horizontal speed multiplier while sprinting.", 1.3, 1.0, 3.0, v -> {}
    ));

    public SpeedModule() {
        super(Category.MOVEMENT, "speed", "Increases horizontal ground movement speed.");
    }

    @Override
    public void onTick() {
        if (mc.player == null || !mc.player.onGround() || !mc.player.isSprinting()) return;

        Vec3 v = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(v.x * multiplier.get(), v.y, v.z * multiplier.get());
    }
}
