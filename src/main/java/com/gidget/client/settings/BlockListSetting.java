package com.gidget.client.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** A grouped checklist of blocks, e.g. for ore or container whitelists. */
public final class BlockListSetting extends Setting<List<Block>> {
    public BlockListSetting(String name, String description, List<Block> defaultValue, Consumer<List<Block>> onChanged) {
        super(name, description, new ArrayList<>(defaultValue), onChanged);
    }

    @Override
    public JsonElement toJson() {
        JsonArray array = new JsonArray();
        for (Block block : value) {
            array.add(BuiltInRegistries.BLOCK.getKey(block).toString());
        }
        return array;
    }

    @Override
    public void fromJson(JsonElement element) {
        List<Block> blocks = new ArrayList<>();
        for (JsonElement entry : element.getAsJsonArray()) {
            Identifier id = Identifier.tryParse(entry.getAsString());
            if (id != null) blocks.add(BuiltInRegistries.BLOCK.getValue(id));
        }
        set(blocks);
    }
}
