package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Bridges automatically: whenever the block directly beneath the player is air, places a block
 * there from the hotbar using a neighboring solid block as the placement reference, the same way
 * a normal right-click placement would.
 */
public final class ScaffoldModule extends Module {
    public ScaffoldModule() {
        super(Category.WORLD, "scaffold", "Automatically places blocks beneath you while walking.");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        BlockPos below = BlockPos.containing(mc.player.getX(), mc.player.getY() - 0.1, mc.player.getZ());
        if (!mc.level.getBlockState(below).isAir()) return;

        Direction reference = findSolidNeighbor(below);
        if (reference == null) return;

        Inventory inventory = mc.player.getInventory();
        int blockSlot = findBlockItem(inventory);
        if (blockSlot == -1) return;

        int previousSlot = inventory.getSelectedSlot();
        if (blockSlot != previousSlot) {
            inventory.setSelectedSlot(blockSlot);
            mc.player.connection.send(new ServerboundSetCarriedItemPacket(blockSlot));
        }

        BlockPos referencePos = below.relative(reference);
        BlockHitResult hit = new BlockHitResult(
            Vec3.atCenterOf(referencePos).relative(reference.getOpposite(), 0.5),
            reference.getOpposite(), referencePos, false
        );
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
    }

    private Direction findSolidNeighbor(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) continue;
            if (!mc.level.getBlockState(pos.relative(dir)).isAir()) return dir;
        }
        return null;
    }

    private int findBlockItem(Inventory inventory) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof BlockItem) return slot;
        }
        return -1;
    }
}
