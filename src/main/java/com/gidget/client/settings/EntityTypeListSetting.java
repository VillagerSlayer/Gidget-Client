package com.gidget.client.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class EntityTypeListSetting extends Setting<List<EntityType<?>>> {
    public EntityTypeListSetting(String name, String description, List<EntityType<?>> defaultValue, Consumer<List<EntityType<?>>> onChanged) {
        super(name, description, new ArrayList<>(defaultValue), onChanged);
    }

    @Override
    public JsonElement toJson() {
        JsonArray array = new JsonArray();
        for (EntityType<?> type : value) {
            array.add(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        }
        return array;
    }

    @Override
    public void fromJson(JsonElement element) {
        List<EntityType<?>> types = new ArrayList<>();
        for (JsonElement entry : element.getAsJsonArray()) {
            Identifier id = Identifier.tryParse(entry.getAsString());
            if (id != null) types.add(BuiltInRegistries.ENTITY_TYPE.getValue(id));
        }
        set(types);
    }
}
