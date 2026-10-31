package net.modcrafters.minecraft.bedandbranch.item;

import net.modcrafters.minecraft.bedandbranch.block.BerryObeliskBlock;
import net.modcrafters.minecraft.bedandbranch.block.CropStickBlock;
import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.modcrafters.minecraft.bedandbranch.block.StickPlant;
import net.modcrafters.minecraft.bedandbranch.block.VineTrellisBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Copies a finished plant onto an empty neighbor, without the 1-in-5. */
public class SpreadStickItem extends StageStickItem {
    public SpreadStickItem(Properties properties) {
        super(0, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!isPlant(state) && context.getClickedFace() == Direction.UP) {
            BlockPos above = pos.above();
            BlockState aboveState = level.getBlockState(above);
            if (isPlant(aboveState)) {
                pos = above;
                state = aboveState;
            }
        }
        if (!isPlant(state)) {
            return InteractionResult.PASS;
        }
        if (CropStickBlock.spreadNow(level, pos, state)
                || BerryObeliskBlock.spreadNow(level, pos, state)
                || VineTrellisBlock.spreadNow(level, pos, state)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    private static boolean isPlant(BlockState state) {
        if (state.is(ModBlocks.CROP_STICK)) {
            return state.getValue(StickPlant.PROPERTY) != StickPlant.NONE;
        }
        if (state.is(ModBlocks.VINE_TRELLIS)) {
            return state.getValue(VineTrellisBlock.AGE) > 0;
        }
        if (state.is(ModBlocks.BERRY_OBELISK)) {
            return state.getValue(BerryObeliskBlock.AGE) > 0;
        }
        return false;
    }
}
