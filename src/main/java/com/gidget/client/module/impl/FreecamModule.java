package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.settings.DoubleSetting;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

/**
 * Ported from Meteor Client's real Freecam: the camera position/rotation are tracked as plain
 * doubles here, completely decoupled from the player entity, and read by CameraMixin (which
 * overrides Camera#alignWithEntity's setPosition/setRotation calls while this is active). The
 * player entity itself is never touched — no position freezing, no camera-entity swap — so there
 * is nothing for vanilla's own movement/render code to fight over, which is what caused the old
 * Marker-based implementation to flicker back and forth between the frozen and drifted position.
 * Movement keys are intercepted and cancelled in KeyboardHandlerMixin before they ever reach
 * vanilla's KeyMapping state, so the real player never receives WASD input while this is active.
 */
public final class FreecamModule extends Module {
    public final DoubleSetting speed = getSettings().add(new DoubleSetting(
        "speed", "Your speed while in freecam.", 1.0, 0.1, 5.0, v -> {}
    ));

    private double x, y, z, prevX, prevY, prevZ;
    private float yaw, pitch, lastYaw, lastPitch;

    private boolean forward, backward, left, right, up, down;

    public FreecamModule() {
        super(Category.RENDER, "freecam", "Detaches the camera to fly around and inspect the world; your real position doesn't move.");
    }

    @Override
    protected void onActivate() {
        if (mc.player == null) return;

        Vec3 camPos = mc.gameRenderer.getMainCamera().position();
        x = prevX = camPos.x;
        y = prevY = camPos.y;
        z = prevZ = camPos.z;

        yaw = lastYaw = mc.player.getYRot();
        pitch = lastPitch = mc.player.getXRot();

        forward = mc.options.keyUp.isDown();
        backward = mc.options.keyDown.isDown();
        left = mc.options.keyLeft.isDown();
        right = mc.options.keyRight.isDown();
        up = mc.options.keyJump.isDown();
        down = mc.options.keyShift.isDown();
        unpress();

        if (mc.levelRenderer != null) mc.levelRenderer.allChanged();
    }

    @Override
    protected void onDeactivate() {
        forward = backward = left = right = up = down = false;
        if (mc.levelRenderer != null) mc.levelRenderer.allChanged();
    }

    /** Also called from MouseHandlerMixin: grabMouse() re-polls every key's raw hardware state via
     *  KeyMapping.setAll(), which would otherwise silently re-enable a movement key still held down. */
    public void unpress() {
        mc.options.keyUp.setDown(false);
        mc.options.keyDown.setDown(false);
        mc.options.keyLeft.setDown(false);
        mc.options.keyRight.setDown(false);
        mc.options.keyJump.setDown(false);
        mc.options.keyShift.setDown(false);
        mc.options.keyAttack.setDown(false);
        mc.options.keyUse.setDown(false);
        mc.options.keyPickItem.setDown(false);
    }

    /** Called from KeyboardHandlerMixin for every raw key event while active. Returns true if consumed. */
    public boolean onMovementKey(KeyEvent event, int action) {
        boolean pressed = action != GLFW.GLFW_RELEASE;

        if (mc.options.keyUp.matches(event)) {
            forward = pressed;
        } else if (mc.options.keyDown.matches(event)) {
            backward = pressed;
        } else if (mc.options.keyLeft.matches(event)) {
            left = pressed;
        } else if (mc.options.keyRight.matches(event)) {
            right = pressed;
        } else if (mc.options.keyJump.matches(event)) {
            up = pressed;
        } else if (mc.options.keyShift.matches(event)) {
            down = pressed;
        } else {
            return false;
        }

        return true;
    }

    /** Redirected here from EntityMixin#turn() instead of updating the real player's rotation, so
     *  the real player's rotation stays exactly where it was when Freecam was toggled on. */
    public void changeLookDirection(double deltaX, double deltaY) {
        lastYaw = yaw;
        lastPitch = pitch;
        yaw += (float) deltaX;
        pitch += (float) deltaY;
        pitch = Mth.clamp(pitch, -90.0F, 90.0F);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        // Defense in depth: MouseHandler#grabMouse() re-polls raw hardware key state via
        // KeyMapping.setAll(), bypassing cancelled key events entirely. Re-forcing this every tick
        // guarantees any such leak self-corrects within one tick regardless of what triggered it.
        unpress();

        Vec3 forwardDir = Vec3.directionFromRotation(0, yaw);
        Vec3 rightDir = Vec3.directionFromRotation(0, yaw + 90.0F);

        double s = (mc.options.keySprint.isDown() ? 1.0 : 0.5) * speed.get();

        double velX = 0;
        double velZ = 0;
        if (forward) {
            velX += forwardDir.x * s;
            velZ += forwardDir.z * s;
        }
        if (backward) {
            velX -= forwardDir.x * s;
            velZ -= forwardDir.z * s;
        }
        if (right) {
            velX += rightDir.x * s;
            velZ += rightDir.z * s;
        }
        if (left) {
            velX -= rightDir.x * s;
            velZ -= rightDir.z * s;
        }

        if ((forward || backward) && (left || right)) {
            double diagonal = 1.0 / Math.sqrt(2.0);
            velX *= diagonal;
            velZ *= diagonal;
        }

        double velY = 0;
        if (up) velY += s;
        if (down) velY -= s;

        prevX = x;
        prevY = y;
        prevZ = z;
        x += velX;
        y += velY;
        z += velZ;
    }

    public double getX(float partialTicks) {
        return Mth.lerp(partialTicks, prevX, x);
    }

    public double getY(float partialTicks) {
        return Mth.lerp(partialTicks, prevY, y);
    }

    public double getZ(float partialTicks) {
        return Mth.lerp(partialTicks, prevZ, z);
    }

    public float getYaw(float partialTicks) {
        return Mth.lerp(partialTicks, lastYaw, yaw);
    }

    public float getPitch(float partialTicks) {
        return Mth.lerp(partialTicks, lastPitch, pitch);
    }
}
