package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.settings.BoolSetting;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class NametagsModule extends Module {
    public final BoolSetting showHealth = getSettings().add(new BoolSetting("health", "Show health.", true, v -> {}));
    public final BoolSetting showDistance = getSettings().add(new BoolSetting("distance", "Show distance.", true, v -> {}));
    public final BoolSetting showHeldItem = getSettings().add(new BoolSetting("held-item", "Show held item.", true, v -> {}));

    public NametagsModule() {
        super(Category.RENDER, "nametags", "Shows player/mob names, health, distance, and held item through walls.");
    }

    @Override
    public void onTick() {
        if (mc.level == null || mc.player == null) return;

        try (var ignored = mc.collectPerTickGizmos()) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (!(entity instanceof LivingEntity living) || entity == mc.player) continue;

                int row = 0;
                Gizmos.billboardTextOverMob(entity, row++, entity.getName().getString(), 0xFFFFFFFF, 1.0F);

                if (showHealth.get()) {
                    String health = String.format("%.1f / %.1f", living.getHealth(), living.getMaxHealth());
                    Gizmos.billboardTextOverMob(entity, row++, health, 0xFFFF5555, 0.8F);
                }

                if (showDistance.get()) {
                    double distance = Math.sqrt(mc.player.distanceToSqr(entity));
                    Gizmos.billboardTextOverMob(entity, row++, String.format("%.1fm", distance), 0xFFAAAAAA, 0.8F);
                }

                if (showHeldItem.get()) {
                    ItemStack held = living.getMainHandItem();
                    if (!held.isEmpty()) {
                        Gizmos.billboardTextOverMob(entity, row, held.getHoverName().getString(), 0xFF55FFFF, 0.8F);
                    }
                }
            }
        }
    }
}
