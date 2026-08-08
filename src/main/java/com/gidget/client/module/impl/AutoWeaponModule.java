package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class AutoWeaponModule extends Module {
    public AutoWeaponModule() {
        super(Category.COMBAT, "auto-weapon", "Switches to your best weapon when attacking a living entity.");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;
        if (!mc.options.keyAttack.isDown()) return;
        if (!(mc.hitResult instanceof EntityHitResult entityHit) || mc.hitResult.getType() != HitResult.Type.ENTITY) return;
        if (!(entityHit.getEntity() instanceof LivingEntity)) return;

        Inventory inventory = mc.player.getInventory();

        int bestSlot = inventory.getSelectedSlot();
        double bestDamage = attackDamage(inventory.getSelectedItem());

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = inventory.getItem(slot);
            double damage = attackDamage(stack);
            if (damage > bestDamage) {
                bestDamage = damage;
                bestSlot = slot;
            }
        }

        if (bestSlot != inventory.getSelectedSlot()) {
            inventory.setSelectedSlot(bestSlot);
            mc.player.connection.send(new ServerboundSetCarriedItemPacket(bestSlot));
        }
    }

    private double attackDamage(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        double[] total = {0};
        stack.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.equals(Attributes.ATTACK_DAMAGE)) total[0] += modifier.amount();
        });
        return total[0];
    }
}
