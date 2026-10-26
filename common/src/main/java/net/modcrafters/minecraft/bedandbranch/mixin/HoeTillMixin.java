package net.modcrafters.minecraft.bedandbranch.mixin;

import net.modcrafters.minecraft.bedandbranch.soil.SoilTilling;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockTransformer.class)
public class HoeTillMixin {
    @Inject(method = "transformBlock", at = @At("HEAD"), cancellable = true)
    private void bedandbranch$tillSoil(UseOnContext context, CallbackInfoReturnable<InteractionResult> callback) {
        SoilTilling.till(context).ifPresent(callback::setReturnValue);
    }
}
