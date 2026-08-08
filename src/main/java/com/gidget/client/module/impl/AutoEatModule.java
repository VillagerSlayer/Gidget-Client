package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.settings.IntSetting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class AutoEatModule extends Module {
    public final IntSetting hungerThreshold = getSettings().add(new IntSetting(
        "hunger-threshold", "Start eating when your hunger drops below this.", 14, 0, 19, v -> {}
    ));

    /** The slot we were on before switching to food, so we can switch back once we're full. -1 when not currently eating. */
    private int originalSlot = -1;

    public AutoEatModule() {
        super(Category.PLAYER, "auto-eat", "Automatically eats food from your hotbar when hungry.");
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        Inventory inventory = mc.player.getInventory();

        if (mc.player.getFoodData().getFoodLevel() >= hungerThreshold.get()) {
            // Don't switch away mid-bite: that would abandon the eat action the server still thinks
            // is in progress. Wait for it to finish, then restore.
            if (!mc.player.isUsingItem()) restoreSlot(inventory);
            return;
        }

        if (mc.player.isUsingItem()) return;

        if (!isFood(inventory.getSelectedItem())) {
            int foodSlot = findFood(inventory);
            if (foodSlot == -1) return;

            if (originalSlot == -1) originalSlot = inventory.getSelectedSlot();
            switchTo(inventory, foodSlot);
        }

        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
    }

    @Override
    protected void onDeactivate() {
        if (mc.player != null) restoreSlot(mc.player.getInventory());
    }

    private void restoreSlot(Inventory inventory) {
        if (originalSlot != -1) {
            switchTo(inventory, originalSlot);
            originalSlot = -1;
        }
    }

    private void switchTo(Inventory inventory, int slot) {
        if (slot == inventory.getSelectedSlot()) return;
        inventory.setSelectedSlot(slot);
        mc.player.connection.send(new ServerboundSetCarriedItemPacket(slot));
    }

    private boolean isFood(ItemStack stack) {
        return !stack.isEmpty() && stack.get(DataComponents.FOOD) != null;
    }

    private int findFood(Inventory inventory) {
        for (int slot = 0; slot < 9; slot++) {
            if (isFood(inventory.getItem(slot))) return slot;
        }
        return -1;
    }
}
