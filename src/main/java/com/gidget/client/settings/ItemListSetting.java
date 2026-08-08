package com.gidget.client.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class ItemListSetting extends Setting<List<Item>> {
    public ItemListSetting(String name, String description, List<Item> defaultValue, Consumer<List<Item>> onChanged) {
        super(name, description, new ArrayList<>(defaultValue), onChanged);
    }

    @Override
    public JsonElement toJson() {
        JsonArray array = new JsonArray();
        for (Item item : value) {
            array.add(BuiltInRegistries.ITEM.getKey(item).toString());
        }
        return array;
    }

    @Override
    public void fromJson(JsonElement element) {
        List<Item> items = new ArrayList<>();
        for (JsonElement entry : element.getAsJsonArray()) {
            Identifier id = Identifier.tryParse(entry.getAsString());
            if (id != null) items.add(BuiltInRegistries.ITEM.getValue(id));
        }
        set(items);
    }
}
