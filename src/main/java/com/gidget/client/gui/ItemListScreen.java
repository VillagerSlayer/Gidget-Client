package com.gidget.client.gui;

import com.gidget.client.settings.ItemListSetting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ItemListScreen extends AbstractSelectListScreen<Item> {
    private final ItemListSetting setting;
    private final List<Item> all = new ArrayList<>();

    public ItemListScreen(Screen parent, ItemListSetting setting) {
        super(parent, Component.literal("Select Items - " + setting.getName()));
        this.setting = setting;
        for (Item item : BuiltInRegistries.ITEM) {
            all.add(item);
        }
    }

    @Override
    protected List<Item> allItems() {
        return all;
    }

    @Override
    protected String idFor(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        return id.toString();
    }

    @Override
    protected ItemStack iconFor(Item item) {
        return new ItemStack(item);
    }

    @Override
    protected List<Item> currentSelection() {
        return setting.get();
    }

    @Override
    protected void setSelection(List<Item> items) {
        setting.set(items);
    }
}
