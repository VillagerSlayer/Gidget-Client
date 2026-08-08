package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.settings.ColorSetting;
import com.gidget.client.settings.DoubleSetting;
import com.gidget.client.settings.EntityTypeListSetting;
import com.gidget.client.settings.GidgetColor;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/** Adapted from Meteor Client's ESP module: per-category colors and a distance fade-out near the edge of range. */
public final class EntityEspModule extends Module {
    public final EntityTypeListSetting entityTypes = getSettings().add(new EntityTypeListSetting(
        "entities", "Which entity types to draw bounding boxes for.",
        List.of(EntityType.PLAYER), v -> {}
    ));

    public final ColorSetting playerColor = getSettings().add(new ColorSetting(
        "player-color", "Color for players.", new GidgetColor(255, 60, 60, 255), v -> {}
    ));

    public final ColorSetting hostileColor = getSettings().add(new ColorSetting(
        "hostile-color", "Color for hostile mobs.", new GidgetColor(255, 160, 0, 255), v -> {}
    ));

    public final ColorSetting passiveColor = getSettings().add(new ColorSetting(
        "passive-color", "Color for animals and other passive mobs.", new GidgetColor(80, 200, 255, 255), v -> {}
    ));

    public final DoubleSetting fadeDistance = getSettings().add(new DoubleSetting(
        "fade-distance", "Distance from the edge of render distance where the box starts fading out.", 8.0, 0.0, 32.0, v -> {}
    ));

    public EntityEspModule() {
        super(Category.RENDER, "entity-esp", "Draws bounding boxes around selected entity types.");
    }

    @Override
    public void onTick() {
        if (mc.level == null || mc.player == null) return;

        List<EntityType<?>> types = entityTypes.get();
        if (types.isEmpty()) return;

        double maxDist = mc.options.renderDistance().get() * 16.0;

        try (var ignored = mc.collectPerTickGizmos()) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity == mc.player) continue;
                if (!types.contains(entity.getType())) continue;

                double dist = Math.sqrt(mc.player.distanceToSqr(entity));
                float alpha = fadeAlpha(dist, maxDist);
                if (alpha <= 0.0F) continue;

                GidgetColor color = colorFor(entity);
                int stroke = new GidgetColor(color.r, color.g, color.b, (int) (color.a * alpha)).toArgb();

                Gizmos.cuboid(entity.getBoundingBox(), GizmoStyle.stroke(stroke, 2.0F)).setAlwaysOnTop();
            }
        }
    }

    private float fadeAlpha(double dist, double maxDist) {
        double fade = fadeDistance.get();
        if (fade <= 0.0 || dist <= maxDist - fade) return 1.0F;
        if (dist >= maxDist) return 0.0F;

        return (float) ((maxDist - dist) / fade);
    }

    private GidgetColor colorFor(Entity entity) {
        if (entity instanceof Player) return playerColor.get();
        if (entity instanceof Monster) return hostileColor.get();
        if (entity instanceof Animal) return passiveColor.get();
        return passiveColor.get();
    }
}
