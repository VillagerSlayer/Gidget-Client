package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

/** Middle-click a container block to open it directly, without needing to right-click. */
public final class MiddleClickExtraModule extends Module {
    private boolean wasDown;

    public MiddleClickExtraModule() {
        super(Category.PLAYER, "middle-click-extra", "Middle-click a container block to open it.");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.screen != null) {
            wasDown = false;
            return;
        }

        boolean down = InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
        boolean pressedThisTick = down && !wasDown;
        wasDown = down;
        if (!pressedThisTick) return;

        if (mc.hitResult instanceof BlockHitResult blockHit && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, blockHit);
        }
    }
}
