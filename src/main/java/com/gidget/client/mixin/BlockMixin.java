package com.gidget.client.mixin;

import com.gidget.client.module.ModuleManager;
import com.gidget.client.module.impl.XrayModule;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true)
    private static void gidget$xrayShouldRenderFace(BlockState state, BlockState neighborState, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        XrayModule xray = ModuleManager.get().get(XrayModule.class);
        if (!xray.isActive()) return;

        // Whitelisted blocks always draw every face (their neighbor may be hidden by the branch below),
        // everything else draws no faces at all, effectively hiding it.
        cir.setReturnValue(xray.isWhitelisted(state.getBlock()));
    }
}
