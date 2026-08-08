package com.gidget.client.mixin;

import com.gidget.client.module.ModuleManager;
import com.gidget.client.module.impl.FreecamModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MouseHandler#grabMouse() calls KeyMapping.setAll(), which re-polls every KeyMapping's isDown
 * state directly from raw hardware input, bypassing our cancelled key events in
 * KeyboardHandlerMixin entirely. This runs whenever the window (re)grabs the mouse (closing a
 * screen, regaining focus, etc.), so a movement key still physically held at that moment would
 * silently start moving the real player again while Freecam is active without this.
 *
 * Also cancels onButton entirely while Freecam is active, matching Meteor's "pure observer"
 * behavior: no attacking, breaking, or interacting while detached from your body.
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Inject(method = "grabMouse", at = @At("TAIL"))
    private void gidget$onGrabMouse(CallbackInfo ci) {
        FreecamModule freecam = ModuleManager.get().get(FreecamModule.class);
        if (freecam.isActive()) {
            freecam.unpress();
        }
    }

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void gidget$onButton(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || !ModuleManager.get().get(FreecamModule.class).isActive()) return;

        // Still allow re-grabbing the mouse on click (e.g. after alt-tabbing back in) so Freecam
        // doesn't lock you out of the window — just skip the attack/use/pick KeyMapping updates.
        MouseHandler self = (MouseHandler) (Object) this;
        if (!self.isMouseGrabbed() && action == GLFW.GLFW_PRESS) {
            self.grabMouse();
        }

        ci.cancel();
    }
}
