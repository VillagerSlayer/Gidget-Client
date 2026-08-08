package com.gidget.client.module.impl;

import com.gidget.client.mixininterface.IMultiPlayerGameMode;
import com.gidget.client.module.Category;
import com.gidget.client.module.Module;

public final class FastBreakModule extends Module {
    public FastBreakModule() {
        super(Category.WORLD, "fast-break", "Removes the delay between finishing one block and starting the next.");
    }

    @Override
    public void onTick() {
        if (mc.gameMode == null) return;
        ((IMultiPlayerGameMode) mc.gameMode).gidget$setDestroyDelay(0);
    }
}
