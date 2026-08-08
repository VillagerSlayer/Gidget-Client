package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;

/** The actual work happens in {@code StepMixin}, which raises Entity#maxUpStep() for the local player while this is active. */
public final class StepModule extends Module {
    public StepModule() {
        super(Category.MOVEMENT, "step", "Lets you walk up full blocks without jumping.");
    }
}
