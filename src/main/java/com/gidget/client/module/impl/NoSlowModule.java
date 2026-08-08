package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;

/** The actual work happens in {@code LocalPlayerMixin}, which skips the item-use speed penalty applied in LocalPlayer#modifyInput. */
public final class NoSlowModule extends Module {
    public NoSlowModule() {
        super(Category.MOVEMENT, "no-slow", "Removes the movement slowdown from eating, blocking, or drawing a bow.");
    }
}
