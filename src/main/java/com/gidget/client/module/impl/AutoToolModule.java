package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class AutoToolModule extends Module {
    public AutoToolModule() {
        super(Category.PLAYER, "auto-tool", "Switches to the best tool in your hotbar for the block you're breaking.");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;
        if (!mc.options.keyAttack.isDown()) return;
        if (!(mc.hitResult instanceof BlockHitResult blockHit) || mc.hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockState state = mc.level.getBlockState(blockHit.getBlockPos());
        Inventory inventory = mc.player.getInventory();

        int bestSlot = inventory.getSelectedSlot();
        float bestSpeed = inventory.getSelectedItem().getDestroySpeed(state);

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            ItemStack stack = inventory.getItem(hotbarSlot);
            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = hotbarSlot;
            }
        }

        if (bestSlot != inventory.getSelectedSlot()) {
            inventory.setSelectedSlot(bestSlot);
            mc.player.connection.send(new ServerboundSetCarriedItemPacket(bestSlot));
        }
    }
}
