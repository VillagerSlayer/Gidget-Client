package com.gidget.client.module.impl;

import com.gidget.client.mixininterface.ILivingEntity;
import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.settings.IntSetting;

/**
 * Shortens the local eating/drinking animation by directly draining LivingEntity#useItemRemaining
 * an extra amount each client tick, the same field vanilla itself counts down. Best-effort: the
 * server tracks item-use duration independently, so a very aggressive value may desync completion.
 */
public final class FastEatModule extends Module {
    public final IntSetting extraTicks = getSettings().add(new IntSetting(
        "extra-ticks", "Extra ticks to drain per client tick.", 2, 1, 5, v -> {}
    ));

    public FastEatModule() {
        super(Category.PLAYER, "fast-eat", "Speeds up eating and drinking food/potions.");
    }

    @Override
    public void onTick() {
        if (mc.player == null || !mc.player.isUsingItem()) return;

        ILivingEntity self = (ILivingEntity) mc.player;
        int remaining = self.gidget$getUseItemRemaining();
        if (remaining <= 0) return;

        self.gidget$setUseItemRemaining(Math.max(0, remaining - extraTicks.get()));
    }
}
