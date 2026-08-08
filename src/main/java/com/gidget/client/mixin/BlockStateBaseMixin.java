package com.gidget.client.mixin;

import com.gidget.client.module.ModuleManager;
import com.gidget.client.module.impl.XrayModule;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * shouldRenderFace-based culling (see BlockMixin) only suppresses faces of standard full-cube
 * models; non-cube models like grass, flowers, and saplings don't go through that path at all and
 * stay visible. Forcing the render shape to INVISIBLE here hides every model type uniformly.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Inject(method = "getRenderShape", at = @At("RETURN"), cancellable = true)
    private void gidget$xrayHideNonWhitelisted(CallbackInfoReturnable<RenderShape> cir) {
        XrayModule xray = ModuleManager.get().get(XrayModule.class);
        if (!xray.isActive()) return;

        BlockBehaviour.BlockStateBase self = (BlockBehaviour.BlockStateBase) (Object) this;
        if (!xray.isWhitelisted(self.getBlock())) {
            cir.setReturnValue(RenderShape.INVISIBLE);
        }
    }
}
