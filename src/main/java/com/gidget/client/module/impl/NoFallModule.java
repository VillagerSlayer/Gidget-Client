package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;

/** The actual work happens in {@code NoFallMixin}, which reports the player as grounded to the server so it never applies fall damage. */
public final class NoFallModule extends Module {
    public NoFallModule() {
        super(Category.MOVEMENT, "no-fall", "Prevents fall damage.");
    }
}
