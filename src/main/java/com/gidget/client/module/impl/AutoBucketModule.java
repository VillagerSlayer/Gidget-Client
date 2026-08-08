package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.settings.IntSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * "Bucket clutch": places a water source directly below the player right before a fall would be
 * lethal, then swaps back to an empty bucket a moment later so the water doesn't linger.
 */
public final class AutoBucketModule extends Module {
    public final IntSetting fallDistanceTrigger = getSettings().add(new IntSetting(
        "fall-distance", "Place water once you've fallen this far.", 8, 3, 30, v -> {}
    ));

    private boolean placed;
    private int ticksSincePlace;

    public AutoBucketModule() {
        super(Category.MOVEMENT, "auto-bucket", "Places water to break a fatal fall, then picks it back up.");
    }

    @Override
    protected void onDeactivate() {
        placed = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        if (placed) {
            if (++ticksSincePlace > 20) {
                pickUpWater();
                placed = false;
            }
            return;
        }

        if (mc.player.onGround() || mc.player.isInWater() || mc.player.isInLava()) return;
        if (mc.player.fallDistance < fallDistanceTrigger.get()) return;
        if (mc.player.getDeltaMovement().y >= 0) return;

        BlockPos below = findLandingSpot();
        if (below == null) return;

        Inventory inventory = mc.player.getInventory();
        int bucketSlot = findItem(inventory, Items.WATER_BUCKET);
        if (bucketSlot == -1) return;

        int previousSlot = inventory.getSelectedSlot();
        if (bucketSlot != previousSlot) {
            inventory.setSelectedSlot(bucketSlot);
            mc.player.connection.send(new ServerboundSetCarriedItemPacket(bucketSlot));
        }

        BlockHitResult hit = new BlockHitResult(
            Vec3.atCenterOf(below).relative(Direction.UP, 0.5), Direction.UP, below, false
        );
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);

        placed = true;
        ticksSincePlace = 0;
    }

    private void pickUpWater() {
        if (mc.player == null || mc.level == null) return;

        BlockPos below = BlockPos.containing(mc.player.getX(), mc.player.getY() - 0.1, mc.player.getZ());
        if (!mc.level.getBlockState(below).is(Blocks.WATER)) return;

        Inventory inventory = mc.player.getInventory();
        int emptyBucketSlot = findItem(inventory, Items.BUCKET);
        if (emptyBucketSlot == -1) return;

        int previousSlot = inventory.getSelectedSlot();
        if (emptyBucketSlot != previousSlot) {
            inventory.setSelectedSlot(emptyBucketSlot);
            mc.player.connection.send(new ServerboundSetCarriedItemPacket(emptyBucketSlot));
        }

        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(below), Direction.UP, below, false);
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
    }

    /** Looks a few blocks straight down from the player for the first solid block to place water on. */
    private BlockPos findLandingSpot() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ());
        for (int i = 0; i < 4; i++) {
            pos.setY(mc.player.getBlockY() - i);
            if (!mc.level.getBlockState(pos).isAir()) return pos.immutable();
        }
        return null;
    }

    private int findItem(Inventory inventory, net.minecraft.world.item.Item item) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) return slot;
        }
        return -1;
    }
}
