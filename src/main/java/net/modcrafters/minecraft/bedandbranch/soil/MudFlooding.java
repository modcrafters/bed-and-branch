package net.modcrafters.minecraft.bedandbranch.soil;

import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Optional;

public final class MudFlooding {
    private MudFlooding() {
    }

    public static Optional<InteractionResult> flood(Player player, InteractionHand hand, BlockHitResult hit) {
        if (player == null || hit.getType() != HitResult.Type.BLOCK) {
            return Optional.empty();
        }
        ItemStack stack = player.getItemInHand(hand);
        Level level = player.level();
        BlockPos pos = hit.getBlockPos();
        if (!level.mayInteract(player, pos)) {
            return Optional.empty();
        }
        if (stack.is(Items.WATER_BUCKET)) {
            return fill(player, hand, level, pos);
        }
        if (stack.is(Items.BUCKET)) {
            return empty(player, hand, level, pos);
        }
        return Optional.empty();
    }

    private static Optional<InteractionResult> fill(Player player, InteractionHand hand, Level level, BlockPos pos) {
        if (!isDryMudFarmland(level, pos)) {
            return Optional.empty();
        }
        if (!level.isClientSide()) {
            BlockState farmland = level.getBlockState(pos);
            level.setBlock(pos, farmland.setValue(SoilState.PROPERTY, SoilState.FLOODED_MUD), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return swapped(player, hand, level, new ItemStack(Items.BUCKET));
    }

    private static Optional<InteractionResult> empty(Player player, InteractionHand hand, Level level, BlockPos pos) {
        if (!isFloodedFarmland(level, pos)) {
            return Optional.empty();
        }
        if (!level.isClientSide()) {
            BlockState farmland = level.getBlockState(pos);
            level.setBlock(pos, farmland.setValue(SoilState.PROPERTY, SoilState.MUD), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return swapped(player, hand, level, new ItemStack(Items.WATER_BUCKET));
    }

    private static Optional<InteractionResult> swapped(Player player, InteractionHand hand, Level level, ItemStack replacement) {
        if (player.hasInfiniteMaterials()) {
            return Optional.of(InteractionResult.SUCCESS);
        }
        //? if >=1.21.2 {
        return Optional.of(InteractionResult.SUCCESS.heldItemTransformedTo(replacement));
        //?} else {
        /*player.setItemInHand(hand, replacement);
        return Optional.of(InteractionResult.sidedSuccess(level.isClientSide()));
        *///?}
    }

    /** Tilled soil on vanilla mud, still dry, so a water bucket can flood it. */
    private static boolean isDryMudFarmland(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(ModBlocks.FARMLAND)
                && state.getValue(SoilState.PROPERTY) == SoilState.MUD
                && level.getBlockState(pos.below()).is(Blocks.MUD);
    }

    private static boolean isFloodedFarmland(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(ModBlocks.FARMLAND) && state.getValue(SoilState.PROPERTY) == SoilState.FLOODED_MUD;
    }
}
