package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Input;

/**
 * Minecraft stops applying movement keys while any screen is open (it assumes you're using the
 * mouse/keyboard for the GUI instead) — but KeyMapping#isDown() keeps tracking physical key state
 * regardless, since that's updated straight from the raw key-press callback. This feeds it back
 * into the player's input each tick, so WASD/space/shift keep working with a container open.
 */
public final class InventoryMoveModule extends Module {
    public InventoryMoveModule() {
        super(Category.MOVEMENT, "inventory-move", "Lets you move and jump while an inventory or chest is open.");
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!(mc.screen instanceof AbstractContainerScreen<?>)) return;

        Input input = new Input(
            mc.options.keyUp.isDown(),
            mc.options.keyDown.isDown(),
            mc.options.keyLeft.isDown(),
            mc.options.keyRight.isDown(),
            mc.options.keyJump.isDown(),
            mc.options.keyShift.isDown(),
            mc.options.keySprint.isDown()
        );

        mc.player.input.keyPresses = input;
        mc.player.input.tick();
    }
}
