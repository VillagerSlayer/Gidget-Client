package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.settings.IntSetting;

public final class AntiAfkModule extends Module {
    public final IntSetting intervalTicks = getSettings().add(new IntSetting(
        "interval", "Ticks between each nudge (20 ticks = 1 second).", 400, 20, 2400, v -> {}
    ));

    private int ticksUntilNudge;

    public AntiAfkModule() {
        super(Category.MISC, "anti-afk", "Nudges your view periodically to avoid AFK-kick detection.");
    }

    @Override
    protected void onActivate() {
        ticksUntilNudge = intervalTicks.get();
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (--ticksUntilNudge > 0) return;
        ticksUntilNudge = intervalTicks.get();

        mc.player.setYRot(mc.player.getYRot() + 1.0F);
        mc.player.setYRot(mc.player.getYRot() - 1.0F);
    }
}
