package com.gidget.client.gui;

import com.gidget.client.settings.BlockListSetting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public final class BlockListScreen extends AbstractSelectListScreen<Block> {
    private final BlockListSetting setting;
    private final List<Block> all = new ArrayList<>();

    public BlockListScreen(Screen parent, BlockListSetting setting) {
        super(parent, Component.literal("Select Blocks - " + setting.getName()));
        this.setting = setting;
        for (Block block : BuiltInRegistries.BLOCK) {
            all.add(block);
        }
    }

    @Override
    protected List<Block> allItems() {
        return all;
    }

    @Override
    protected String idFor(Block item) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(item);
        return id.toString();
    }

    @Override
    protected ItemStack iconFor(Block item) {
        return new ItemStack(item.asItem());
    }

    @Override
    protected List<Block> currentSelection() {
        return setting.get();
    }

    @Override
    protected void setSelection(List<Block> items) {
        setting.set(items);
    }
}
