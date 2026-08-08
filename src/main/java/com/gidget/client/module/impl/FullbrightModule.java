package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;

public final class FullbrightModule extends Module {
    public FullbrightModule() {
        super(Category.RENDER, "fullbright", "Forces the lightmap's ambient color to white.");
    }
}
