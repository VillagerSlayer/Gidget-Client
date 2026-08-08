package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.settings.DoubleSetting;

public final class SpiderModule extends Module {
    public final DoubleSetting climbSpeed = getSettings().add(new DoubleSetting(
        "climb-speed", "Vertical speed while pressed against a wall.", 0.2, 0.05, 0.5, v -> {}
    ));

    public SpiderModule() {
        super(Category.MOVEMENT, "spider", "Lets you climb walls by walking into them.");
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        var input = mc.player.input.keyPresses;
        boolean movingIntoWall = mc.player.horizontalCollision
            && (input.forward() || input.backward() || input.left() || input.right());

        if (movingIntoWall) {
            var v = mc.player.getDeltaMovement();
            mc.player.setDeltaMovement(v.x, climbSpeed.get(), v.z);
            mc.player.fallDistance = 0;
        }
    }
}
