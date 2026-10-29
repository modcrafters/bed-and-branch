package net.modcrafters.minecraft.bedandbranch.mixin;

import net.modcrafters.minecraft.bedandbranch.block.CropStickBlock;
import net.modcrafters.minecraft.bedandbranch.block.SoilFarmlandBlock;
import net.modcrafters.minecraft.bedandbranch.item.SeedPlanting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
//? if <1.21.2
//import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public class FarmlandStickMixin {
    // Vanilla farmland has no use handler of its own. Our tilled soil already forwards a click
    // to the stick above it; this does the same for the vanilla block.
    //? if >=1.21.2 {
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void bedandbranch$useStickOnFarmland(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> callback
    ) {
        InteractionResult handled = stickAbove(stack, state, level, pos, player, hit);
        if (handled != null) {
            callback.setReturnValue(handled);
        }
    }
    //?} else {
    /*@Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void bedandbranch$useStickOnFarmland(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<ItemInteractionResult> callback
    ) {
        InteractionResult handled = stickAbove(stack, state, level, pos, player, hit);
        if (handled != null) {
            callback.setReturnValue(SeedPlanting.asItem(handled));
        }
    }
    *///?}

    private static InteractionResult stickAbove(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (!(state.getBlock() instanceof FarmlandBlock) || state.getBlock() instanceof SoilFarmlandBlock) {
            return null;
        }
        if (hit.getDirection() != Direction.UP) {
            return null;
        }
        return CropStickBlock.usePlantedStick(stack, level, pos.above(), player);
    }
}
