package com.gidget.client.mixin;

import com.gidget.client.mixininterface.IMultiPlayerGameMode;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin implements IMultiPlayerGameMode {
    @Accessor("destroyDelay")
    public abstract void setDestroyDelay(int delay);

    @Override
    public void gidget$setDestroyDelay(int delay) {
        setDestroyDelay(delay);
    }
}
