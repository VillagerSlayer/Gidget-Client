package com.gidget.client.mixin;

import com.gidget.client.mixininterface.ILivingEntity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements ILivingEntity {
    @Accessor("useItemRemaining")
    @Override
    public abstract int gidget$getUseItemRemaining();

    @Accessor("useItemRemaining")
    @Override
    public abstract void gidget$setUseItemRemaining(int ticks);
}
