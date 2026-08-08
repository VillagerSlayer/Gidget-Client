package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;

public final class AutoSprintModule extends Module {
    public AutoSprintModule() {
        super(Category.MOVEMENT, "auto-sprint", "Sprints continuously while moving forward, without double-tapping.");
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.player.input.hasForwardImpulse() && !mc.player.isCrouching()) {
            mc.options.keySprint.setDown(true);
        }
    }

    @Override
    protected void onDeactivate() {
        mc.options.keySprint.setDown(false);
    }
}
