package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.util.InventoryUtils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public final class ChestStealerModule extends Module {
    private AbstractContainerMenu lastEmptied;

    public ChestStealerModule() {
        super(Category.PLAYER, "chest-stealer", "Instantly quick-moves every item out of an opened container.");
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!(mc.screen instanceof AbstractContainerScreen<?> containerScreen)) return;

        AbstractContainerMenu menu = containerScreen.getMenu();
        if (menu == mc.player.inventoryMenu || menu == lastEmptied) return;

        for (Slot slot : menu.slots) {
            if (slot.container != mc.player.getInventory() && slot.hasItem()) {
                InventoryUtils.quickMove(mc.player, slot.index);
            }
        }

        lastEmptied = menu;
    }
}
