package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.settings.DoubleSetting;

public final class FlyModule extends Module {
    public final DoubleSetting speed = getSettings().add(new DoubleSetting(
        "speed", "Blocks per tick.", 0.5, 0.05, 3.0, v -> {}
    ));

    public FlyModule() {
        super(Category.MOVEMENT, "fly", "Lets you fly freely, ignoring gravity.");
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        var input = mc.player.input.keyPresses;
        double forward = (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0);
        double strafe = (input.right() ? 1 : 0) - (input.left() ? 1 : 0);
        double vertical = (input.jump() ? 1 : 0) - (input.shift() ? 1 : 0);

        float yaw = mc.player.getYRot();
        double sinYaw = Math.sin(Math.toRadians(yaw));
        double cosYaw = Math.cos(Math.toRadians(yaw));

        double dx = strafe * cosYaw - forward * sinYaw;
        double dz = forward * cosYaw + strafe * sinYaw;
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length > 0) {
            dx = dx / length * speed.get();
            dz = dz / length * speed.get();
        }

        mc.player.setDeltaMovement(dx, vertical * speed.get(), dz);
        mc.player.fallDistance = 0;
    }
}
