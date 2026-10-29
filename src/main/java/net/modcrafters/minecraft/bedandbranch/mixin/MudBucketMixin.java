package net.modcrafters.minecraft.bedandbranch.mixin;

import net.modcrafters.minecraft.bedandbranch.soil.MudFlooding;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
//? if <1.21.2
//import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public abstract class MudBucketMixin extends Item {
    private MudBucketMixin(Properties properties) {
        super(properties);
    }

    // 1.21.2+ item use returns InteractionResult. 1.21.1 still returns InteractionResultHolder, and handing it
    // an InteractionResult crashes with a ClassCastException inside BucketItem.use.
    //? if >=1.21.2 {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void bedandbranch$floodMud(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> callback) {
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        MudFlooding.flood(player, hand, hit).ifPresent(callback::setReturnValue);
    }
    //?} else {
    /*@Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void bedandbranch$floodMud(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> callback) {
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        MudFlooding.flood(player, hand, hit).ifPresent(result ->
                callback.setReturnValue(new InteractionResultHolder<>(result, player.getItemInHand(hand))));
    }
    *///?}
}
