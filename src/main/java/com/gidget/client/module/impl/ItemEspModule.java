package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.settings.ColorSetting;
import com.gidget.client.settings.GidgetColor;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

public final class ItemEspModule extends Module {
    public final ColorSetting color = getSettings().add(new ColorSetting(
        "color", "Highlight color.", new GidgetColor(255, 215, 0, 255), v -> {}
    ));

    public ItemEspModule() {
        super(Category.RENDER, "item-esp", "Highlights dropped items through walls.");
    }

    @Override
    public void onTick() {
        if (mc.level == null) return;

        GizmoStyle style = GizmoStyle.strokeAndFill(color.get().toArgb(), 1.5F, withAlpha(color.get(), 60));

        try (var ignored = mc.collectPerTickGizmos()) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity instanceof ItemEntity) {
                    Gizmos.cuboid(entity.getBoundingBox(), style).setAlwaysOnTop();
                }
            }
        }
    }

    private static int withAlpha(GidgetColor c, int alpha) {
        return new GidgetColor(c.r, c.g, c.b, alpha).toArgb();
    }
}
