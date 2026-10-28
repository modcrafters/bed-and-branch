package net.modcrafters.minecraft.bedandbranch.soil;

import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.modcrafters.minecraft.bedandbranch.block.MudBlock;
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
        BlockPos mudPos = mudPos(level, pos);
        if (mudPos == null || !level.mayInteract(player, mudPos)) {
            return Optional.empty();
        }
        BlockState mud = level.getBlockState(mudPos);
        if (!level.isClientSide()) {
            BlockState flooded = ModBlocks.MUD.defaultBlockState().setValue(MudBlock.WATERLOGGED, true);
            if (mud.is(ModBlocks.MUD)) {
                flooded = mud.setValue(MudBlock.WATERLOGGED, true);
            }
            level.setBlock(mudPos, flooded, Block.UPDATE_ALL);
            level.playSound(null, mudPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return swapped(player, hand, level, new ItemStack(Items.BUCKET));
    }

    private static Optional<InteractionResult> empty(Player player, InteractionHand hand, Level level, BlockPos pos) {
        BlockPos mudPos = floodedPos(level, pos);
        if (mudPos == null || !level.mayInteract(player, mudPos)) {
            return Optional.empty();
        }
        if (!level.isClientSide()) {
            BlockState mud = level.getBlockState(mudPos);
            level.setBlock(mudPos, mud.setValue(MudBlock.WATERLOGGED, false), Block.UPDATE_ALL);
            level.playSound(null, mudPos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
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

    private static BlockPos mudPos(Level level, BlockPos pos) {
        if (isDryMud(level.getBlockState(pos))) {
            return pos;
        }
        if (level.getBlockState(pos).is(ModBlocks.FARMLAND) && isDryMud(level.getBlockState(pos.below()))) {
            return pos.below();
        }
        return null;
    }

    private static BlockPos floodedPos(Level level, BlockPos pos) {
        if (isFlooded(level.getBlockState(pos))) {
            return pos;
        }
        if (level.getBlockState(pos).is(ModBlocks.FARMLAND) && isFlooded(level.getBlockState(pos.below()))) {
            return pos.below();
        }
        return null;
    }

    private static boolean isDryMud(BlockState state) {
        if (state.is(Blocks.MUD)) {
            return true;
        }
        return state.is(ModBlocks.MUD) && !state.getValue(MudBlock.WATERLOGGED);
    }

    private static boolean isFlooded(BlockState state) {
        return state.is(ModBlocks.MUD) && state.getValue(MudBlock.WATERLOGGED);
    }
}
