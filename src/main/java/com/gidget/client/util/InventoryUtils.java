package com.gidget.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

/**
 * Slot manipulation via {@code MultiPlayerGameMode#handleContainerInput}, the same entry point a
 * screen calls on a real mouse click — unlike calling {@code AbstractContainerMenu#clicked}
 * directly, this both applies the local client-side prediction AND sends the corresponding
 * ServerboundContainerClickPacket, so the server's inventory actually agrees with what you see.
 * (Confirmed against Meteor Client's real InvUtils, which goes through the same method.)
 *
 * Slot indices below are the vanilla player inventory menu's fixed layout (unchanged for years):
 * 0 = crafting result, 1-4 = crafting grid, 5-8 = armor (helmet..boots), 9-35 = main inventory,
 * 36-44 = hotbar, 45 = offhand.
 */
public final class InventoryUtils {
    public static final int SLOT_HELMET = 5;
    public static final int SLOT_CHESTPLATE = 6;
    public static final int SLOT_LEGGINGS = 7;
    public static final int SLOT_BOOTS = 8;
    public static final int MAIN_START = 9;
    public static final int MAIN_END = 35;
    public static final int HOTBAR_START = 36;
    public static final int HOTBAR_END = 44;
    public static final int SLOT_OFFHAND = 45;

    private InventoryUtils() {
    }

    public static int armorSlotFor(net.minecraft.world.entity.EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> SLOT_HELMET;
            case CHEST -> SLOT_CHESTPLATE;
            case LEGS -> SLOT_LEGGINGS;
            case FEET -> SLOT_BOOTS;
            default -> throw new IllegalArgumentException("Not an armor slot: " + slot);
        };
    }

    private static void click(LocalPlayer player, int containerId, int slotIndex, int button, ContainerInput type) {
        Minecraft.getInstance().gameMode.handleContainerInput(containerId, slotIndex, button, type, player);
    }

    /** Shift-click a slot in whatever menu is currently open (e.g. a chest), moving its stack to the player's inventory. */
    public static void quickMove(LocalPlayer player, int slotIndex) {
        click(player, player.containerMenu.containerId, slotIndex, 0, ContainerInput.QUICK_MOVE);
    }

    /** Swaps the contents of two slots in the player's own inventory menu, as if manually dragged. */
    public static void swap(LocalPlayer player, int fromSlot, int toSlot) {
        if (fromSlot == toSlot) return;

        int containerId = player.inventoryMenu.containerId;
        ItemStack destBefore = player.inventoryMenu.getSlot(toSlot).getItem().copy();

        click(player, containerId, fromSlot, 0, ContainerInput.PICKUP);
        click(player, containerId, toSlot, 0, ContainerInput.PICKUP);
        if (!destBefore.isEmpty()) {
            click(player, containerId, fromSlot, 0, ContainerInput.PICKUP);
        }
    }

    /** Throws a single slot's stack out of the player's own inventory. */
    public static void drop(LocalPlayer player, int slotIndex) {
        click(player, player.inventoryMenu.containerId, slotIndex, 1, ContainerInput.THROW);
    }
}
