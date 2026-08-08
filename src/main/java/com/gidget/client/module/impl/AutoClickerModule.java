package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.settings.BoolSetting;
import com.gidget.client.settings.EnumSetting;
import com.gidget.client.settings.IntSetting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Mirrors a standalone autoclicker's settings (interval, mouse button, single/double, repeat count
 * vs. until-stopped). No default keybind is set — like every other module, the toggle key is left
 * unbound until you assign one yourself in the module's settings.
 */
public final class AutoClickerModule extends Module {
    public enum MouseButtonOption { LEFT, RIGHT }

    public enum ClickTypeOption { SINGLE, DOUBLE }

    public final IntSetting hours = getSettings().add(new IntSetting(
        "hours", "Click interval — hours.", 0, 0, 24, v -> {}
    ));
    public final IntSetting minutes = getSettings().add(new IntSetting(
        "minutes", "Click interval — minutes.", 0, 0, 59, v -> {}
    ));
    public final IntSetting seconds = getSettings().add(new IntSetting(
        "seconds", "Click interval — seconds.", 0, 0, 59, v -> {}
    ));
    public final IntSetting milliseconds = getSettings().add(new IntSetting(
        "milliseconds", "Click interval — milliseconds.", 100, 0, 999, v -> {}
    ));

    public final EnumSetting<MouseButtonOption> mouseButton = getSettings().add(new EnumSetting<>(
        "mouse-button", "Which button to click.", MouseButtonOption.class, MouseButtonOption.LEFT, v -> {}
    ));
    public final EnumSetting<ClickTypeOption> clickType = getSettings().add(new EnumSetting<>(
        "click-type", "Single or double click each interval.", ClickTypeOption.class, ClickTypeOption.SINGLE, v -> {}
    ));

    public final BoolSetting repeatUntilStopped = getSettings().add(new BoolSetting(
        "repeat-until-stopped", "Keep clicking until toggled off, instead of stopping after a fixed count.", true, v -> {}
    ));
    public final IntSetting repeatCount = getSettings().add(new IntSetting(
        "repeat-count", "Number of clicks before auto-stopping (when not repeating until stopped).", 1, 1, 1_000_000, v -> {}
    ));

    private long lastClickTime;
    private int clicksDone;

    public AutoClickerModule() {
        super(Category.MISC, "auto-clicker", "Automatically clicks at a set interval.");
    }

    @Override
    protected void onActivate() {
        lastClickTime = System.currentTimeMillis();
        clicksDone = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) return;

        long interval = ((long) hours.get() * 3600 + (long) minutes.get() * 60 + seconds.get()) * 1000 + milliseconds.get();
        if (interval <= 0) interval = 1;

        long now = System.currentTimeMillis();
        if (now - lastClickTime < interval) return;
        lastClickTime = now;

        performClick();
        if (clickType.get() == ClickTypeOption.DOUBLE) performClick();

        if (!repeatUntilStopped.get()) {
            clicksDone++;
            if (clicksDone >= repeatCount.get()) {
                setActive(false);
            }
        }
    }

    private void performClick() {
        if (mouseButton.get() == MouseButtonOption.LEFT) {
            performLeftClick();
        } else {
            performRightClick();
        }
        mc.player.swing(InteractionHand.MAIN_HAND);
    }

    private void performLeftClick() {
        HitResult hit = mc.hitResult;
        if (hit instanceof EntityHitResult entityHit) {
            mc.gameMode.attack(mc.player, entityHit.getEntity());
        } else if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            mc.gameMode.startDestroyBlock(blockHit.getBlockPos(), blockHit.getDirection());
        }
    }

    private void performRightClick() {
        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, blockHit);
        } else {
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        }
    }
}
