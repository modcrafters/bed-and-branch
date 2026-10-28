package net.modcrafters.minecraft.bedandbranch.mixin;

import net.modcrafters.minecraft.bedandbranch.soil.SoilTilling;
//? if >=26.1
import net.minecraft.core.component.BlockTransformer;
//? if <26.1
//import net.minecraft.world.item.HoeItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Hoes till through BlockTransformer since 26.1. 1.21.1 still uses HoeItem.
//? if >=26.1 {
@Mixin(BlockTransformer.class)
//?} else
//@Mixin(HoeItem.class)
public class HoeTillMixin {
    //? if >=26.1 {
    @Inject(method = "transformBlock", at = @At("HEAD"), cancellable = true)
    //?} else
    //@Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void bedandbranch$tillSoil(UseOnContext context, CallbackInfoReturnable<InteractionResult> callback) {
        SoilTilling.till(context).ifPresent(callback::setReturnValue);
    }
}
