package net.modcrafters.minecraft.bedandbranch.mixin;

import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.minecraft.core.BlockPos;
//? if >=26.1
import net.minecraft.server.level.ServerLevel;
//? if <26.1
//import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FlowingFluid.class)
public class ContainedWaterMixin {
    // 26.1 passes the block the fluid is in. 1.21.1's tick only has the fluid, so the block is read from the level.
    //? if >=26.1 {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void bedandbranch$keepMudWaterContained(ServerLevel level, BlockPos pos, BlockState state, FluidState fluid, CallbackInfo callback) {
        if (state.is(ModBlocks.MUD) && fluid.getType() == Fluids.WATER) {
            callback.cancel();
        }
    }
    //?} else {
    /*@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void bedandbranch$keepMudWaterContained(Level level, BlockPos pos, FluidState fluid, CallbackInfo callback) {
        if (level.getBlockState(pos).is(ModBlocks.MUD) && fluid.getType() == Fluids.WATER) {
            callback.cancel();
        }
    }
    *///?}
}
