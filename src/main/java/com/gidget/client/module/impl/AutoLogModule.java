package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.settings.DoubleSetting;

public final class AutoLogModule extends Module {
    public final DoubleSetting healthThreshold = getSettings().add(new DoubleSetting(
        "health-threshold", "Disconnect once your health drops to or below this.", 6.0, 0.0, 20.0, v -> {}
    ));

    public AutoLogModule() {
        super(Category.PLAYER, "auto-log", "Disconnects automatically if your health drops too low.");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;
        if (mc.player.getHealth() > healthThreshold.get()) return;

        mc.disconnectWithProgressScreen();
    }
}
