package net.modcrafters.minecraft.bedandbranch.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
//? if <1.21.2
//import net.minecraft.world.entity.Entity;
//? if <1.21.2
//import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
//? if >=1.21.2
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;

public class NetherFarmlandBlock extends FarmlandBlock {
    public NetherFarmlandBlock(Properties properties) {
        // The block this farmland turns back into. 1.21.1 still hardcodes dirt, so fall and drying set it themselves.
        //? if >=26.1 {
        super(Blocks.NETHERRACK, properties);
        //?} else
        //super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!hasSoulSand(context.getLevel(), context.getClickedPos())) {
            return null;
        }
        return super.getStateForPlacement(context);
    }

    //? if >=1.21.2 {
    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        if (!hasSoulSand(level, pos)) {
            return Blocks.NETHERRACK.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);
    }
    //?} else {
    /*@Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!hasSoulSand(level, pos)) {
            return Blocks.NETHERRACK.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
    *///?}

    // 1.21.1 always turns farmland into dirt. Later versions use the block passed to the constructor.
    //? if <1.21.2 {
    /*@Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (!level.isClientSide()
                && level.random.nextFloat() < fallDistance - 0.5F
                && entity instanceof Player
                && entity.getBbWidth() * entity.getBbWidth() * entity.getBbHeight() > 0.512F) {
            level.setBlock(pos, Blocks.NETHERRACK.defaultBlockState(), Block.UPDATE_ALL);
        }
        entity.causeFallDamage(fallDistance, 1.0F, level.damageSources().fall());
    }
    *///?}

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!hasSoulSand(level, pos)) {
            level.setBlock(pos, Blocks.NETHERRACK.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }
        int moisture = state.getValue(MOISTURE);
        if (nearLava(level, pos)) {
            if (moisture < MAX_MOISTURE) {
                level.setBlock(pos, state.setValue(MOISTURE, MAX_MOISTURE), Block.UPDATE_CLIENTS);
            }
            return;
        }
        if (moisture > 0) {
            level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), Block.UPDATE_CLIENTS);
        } else if (!level.getBlockState(pos.above()).is(BlockTags.MAINTAINS_FARMLAND)) {
            level.setBlock(pos, Blocks.NETHERRACK.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    /** Lava within the same reach water uses on ordinary farmland. That fill is the fertilization. */
    public static boolean nearLava(LevelReader level, BlockPos pos) {
        for (BlockPos cursor : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
            if (level.getFluidState(cursor).is(FluidTags.LAVA)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasSoulSand(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(Blocks.SOUL_SAND);
    }
}
