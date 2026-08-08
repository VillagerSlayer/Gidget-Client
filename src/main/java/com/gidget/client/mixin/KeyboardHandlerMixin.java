package com.gidget.client.mixin;

import com.gidget.client.gui.ClickGuiScreen;
import com.gidget.client.macro.MacroManager;
import com.gidget.client.module.ModuleManager;
import com.gidget.client.module.impl.FreecamModule;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void gidget$onKeyPress(long handle, int action, KeyEvent event, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        // Deliberately does NOT skip GLFW_REPEAT here: if a movement key was already held down
        // before Freecam activated, there's no fresh PRESS event to cancel — only an ongoing
        // stream of REPEAT events for that key, which still reach KeyMapping.set(key, true) in
        // vanilla's uncancelled body and silently re-arm movement if let through.
        if (mc.screen == null) {
            FreecamModule freecam = ModuleManager.get().get(FreecamModule.class);
            if (freecam.isActive() && freecam.onMovementKey(event, action)) {
                ci.cancel();
                return;
            }
        }

        if (action != GLFW.GLFW_PRESS) return;

        if (mc.screen != null) return;

        if (event.key() == GLFW.GLFW_KEY_RIGHT_CONTROL || event.key() == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            mc.setScreen(new ClickGuiScreen());
            return;
        }

        ModuleManager.get().onKeyPressed(event.key());
        MacroManager.get().onKeyPressed(event.key());
    }
}
