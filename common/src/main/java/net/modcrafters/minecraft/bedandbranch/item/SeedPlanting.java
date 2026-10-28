package net.modcrafters.minecraft.bedandbranch.item;

import net.modcrafters.minecraft.bedandbranch.block.CropStickBlock;
import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.modcrafters.minecraft.bedandbranch.block.SoilFarmlandBlock;
import net.modcrafters.minecraft.bedandbranch.block.StickPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class SeedPlanting {
    private SeedPlanting() {
    }

    public static InteractionResult tryPlant(Level level, Player player, ItemStack stack, BlockPos pos) {
        if (!(level.getBlockState(pos.below()).getBlock() instanceof SoilFarmlandBlock)) {
            return InteractionResult.PASS;
        }
        BlockState existing = level.getBlockState(pos);
        if (existing.is(ModBlocks.CROP_STICK)) {
            return plantOnStick(level, player, stack, pos, existing);
        }
        if (!existing.canBeReplaced()) {
            return InteractionResult.PASS;
        }
        BlockState crop = cropState(stack);
        if (crop == null || !crop.canSurvive(level, pos)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, crop, Block.UPDATE_ALL);
            if (player == null || !player.hasInfiniteMaterials()) {
                stack.shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult plantOnStick(Level level, Player player, ItemStack stack, BlockPos pos, BlockState stick) {
        if (stick.getValue(StickPlant.PROPERTY) != StickPlant.NONE) {
            return InteractionResult.PASS;
        }
        StickPlant plant = StickPlant.fromSeed(stack);
        if (plant == null || !plant.survives(level, pos)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, stick.setValue(StickPlant.PROPERTY, plant).setValue(CropStickBlock.AGE, 0), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (player == null || !player.hasInfiniteMaterials()) {
                stack.shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static BlockState cropState(ItemStack stack) {
        if (stack.getItem() instanceof TomatoSeedsItem) {
            return ModBlocks.TOMATO.defaultBlockState();
        }
        if (stack.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof CropBlock || block instanceof StemBlock) {
                return block.defaultBlockState();
            }
        }
        return null;
    }
}
