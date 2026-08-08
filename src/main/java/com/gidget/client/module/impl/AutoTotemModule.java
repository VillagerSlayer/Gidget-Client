package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.util.InventoryUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AutoTotemModule extends Module {
    public AutoTotemModule() {
        super(Category.PLAYER, "auto-totem", "Keeps a Totem of Undying in your offhand whenever you have one.");
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return;

        for (int slot = InventoryUtils.MAIN_START; slot <= InventoryUtils.HOTBAR_END; slot++) {
            ItemStack stack = mc.player.inventoryMenu.getSlot(slot).getItem();
            if (stack.is(Items.TOTEM_OF_UNDYING)) {
                InventoryUtils.swap(mc.player, slot, InventoryUtils.SLOT_OFFHAND);
                return;
            }
        }
    }
}
