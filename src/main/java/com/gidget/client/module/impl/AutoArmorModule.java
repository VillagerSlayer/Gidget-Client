package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.util.InventoryUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.ItemStack;

public final class AutoArmorModule extends Module {
    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private int ticksUntilCheck;

    public AutoArmorModule() {
        super(Category.PLAYER, "auto-armor", "Equips the best armor in your inventory into each armor slot.");
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (--ticksUntilCheck > 0) return;
        ticksUntilCheck = CHECK_INTERVAL_TICKS;

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            double equippedValue = armorValue(mc.player.getItemBySlot(slot), slot);
            int bestInventorySlot = -1;
            double bestValue = equippedValue;

            for (int i = InventoryUtils.MAIN_START; i <= InventoryUtils.HOTBAR_END; i++) {
                ItemStack stack = mc.player.inventoryMenu.getSlot(i).getItem();
                if (stack.isEmpty()) continue;

                double value = armorValue(stack, slot);
                if (value > bestValue) {
                    bestValue = value;
                    bestInventorySlot = i;
                }
            }

            if (bestInventorySlot != -1) {
                InventoryUtils.swap(mc.player, bestInventorySlot, InventoryUtils.armorSlotFor(slot));
            }
        }
    }

    /** Armor value for the given slot, or -1 if the item can't go there at all. */
    private double armorValue(ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty()) return 0;

        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.slot() != slot) return -1;

        double[] total = {0};
        stack.forEachModifier(slot, (attribute, modifier) -> {
            if (attribute.equals(Attributes.ARMOR)) {
                total[0] += modifier.amount();
            }
        });
        return total[0];
    }
}
