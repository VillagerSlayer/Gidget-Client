package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.settings.ItemListSetting;
import com.gidget.client.util.InventoryUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class InventoryCleanerModule extends Module {
    private static final int CHECK_INTERVAL_TICKS = 20;

    public final ItemListSetting blacklist = getSettings().add(new ItemListSetting(
        "blacklist", "Items to automatically drop.", List.of(Items.COBBLESTONE, Items.DIRT, Items.ROTTEN_FLESH), v -> {}
    ));

    private int ticksUntilCheck;

    public InventoryCleanerModule() {
        super(Category.PLAYER, "inventory-cleaner", "Drops blacklisted junk items from your inventory.");
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (--ticksUntilCheck > 0) return;
        ticksUntilCheck = CHECK_INTERVAL_TICKS;

        List<net.minecraft.world.item.Item> junk = blacklist.get();
        if (junk.isEmpty()) return;

        for (int slot = InventoryUtils.MAIN_START; slot <= InventoryUtils.HOTBAR_END; slot++) {
            ItemStack stack = mc.player.inventoryMenu.getSlot(slot).getItem();
            if (!stack.isEmpty() && junk.contains(stack.getItem())) {
                InventoryUtils.drop(mc.player, slot);
            }
        }
    }
}
