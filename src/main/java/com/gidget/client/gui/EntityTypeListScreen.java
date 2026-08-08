package com.gidget.client.gui;

import com.gidget.client.settings.EntityTypeListSetting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public final class EntityTypeListScreen extends AbstractSelectListScreen<EntityType<?>> {
    private final EntityTypeListSetting setting;
    private final List<EntityType<?>> all = new ArrayList<>();

    public EntityTypeListScreen(Screen parent, EntityTypeListSetting setting) {
        super(parent, Component.literal("Select Entities - " + setting.getName()));
        this.setting = setting;
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            all.add(type);
        }
    }

    @Override
    protected List<EntityType<?>> allItems() {
        return all;
    }

    @Override
    protected String idFor(EntityType<?> item) {
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(item);
        return id.toString();
    }

    @Override
    protected List<EntityType<?>> currentSelection() {
        return setting.get();
    }

    @Override
    protected void setSelection(List<EntityType<?>> items) {
        setting.set(items);
    }
}
