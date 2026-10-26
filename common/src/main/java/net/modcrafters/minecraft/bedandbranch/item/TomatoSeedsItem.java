package net.modcrafters.minecraft.bedandbranch.item;

import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.modcrafters.minecraft.bedandbranch.block.SoilFarmlandBlock;
import net.modcrafters.minecraft.bedandbranch.block.TomatoCropBlock;
import net.modcrafters.minecraft.bedandbranch.soil.SoilLookup;
import net.modcrafters.minecraft.bedandbranch.soil.SoilRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TomatoSeedsItem extends Item {
    public TomatoSeedsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.is(ModBlocks.CROP_STICK)) {
            return plant(context, pos, true);
        }
        if (state.getBlock() instanceof SoilFarmlandBlock && context.getClickedFace() == Direction.UP) {
            BlockPos above = pos.above();
            if (level.getBlockState(above).canBeReplaced()) {
                return plant(context, above, false);
            }
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult plant(UseOnContext context, BlockPos pos, boolean staked) {
        Level level = context.getLevel();
        BlockPos farmPos = pos.below();
        if (!(level.getBlockState(farmPos).getBlock() instanceof SoilFarmlandBlock)) {
            return InteractionResult.PASS;
        }
        if (!SoilRules.tomatoSurvives(true, SoilLookup.kindOf(level.getBlockState(farmPos.below())).orElse(null))) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            BlockState crop = ModBlocks.TOMATO.defaultBlockState().setValue(TomatoCropBlock.STAKED, staked);
            level.setBlock(pos, crop, Block.UPDATE_ALL);
            if (context.getPlayer() == null || !context.getPlayer().hasInfiniteMaterials()) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
