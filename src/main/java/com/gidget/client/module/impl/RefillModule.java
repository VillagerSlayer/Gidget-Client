package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.util.InventoryUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** When a hotbar slot that held potions/food/pearls/etc runs out, pulls a matching stack from the rest of your inventory to replace it. */
public final class RefillModule extends Module {
    private static final int CHECK_INTERVAL_TICKS = 10;

    private final Item[] lastSeenInSlot = new Item[9];
    private int ticksUntilCheck;

    public RefillModule() {
        super(Category.PLAYER, "refill", "Refills empty hotbar slots (potions, food, pearls, etc) from your inventory.");
    }

    @Override
    protected void onActivate() {
        java.util.Arrays.fill(lastSeenInSlot, null);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        Inventory inventory = mc.player.getInventory();

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            ItemStack current = inventory.getItem(hotbarSlot);
            if (!current.isEmpty()) {
                lastSeenInSlot[hotbarSlot] = current.getItem();
            }
        }

        if (--ticksUntilCheck > 0) return;
        ticksUntilCheck = CHECK_INTERVAL_TICKS;

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            if (!inventory.getItem(hotbarSlot).isEmpty()) continue;

            Item wanted = lastSeenInSlot[hotbarSlot];
            if (wanted == null || wanted == Items.AIR) continue;

            int sourceSlot = findItem(inventory, wanted);
            if (sourceSlot != -1) {
                InventoryUtils.swap(mc.player, sourceSlot, InventoryUtils.HOTBAR_START + hotbarSlot);
            }
        }
    }

    private int findItem(Inventory inventory, Item item) {
        for (int slot = InventoryUtils.MAIN_START; slot <= InventoryUtils.MAIN_END; slot++) {
            if (inventory.getItem(slot).is(item)) return slot;
        }
        return -1;
    }
}
