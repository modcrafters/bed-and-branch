package net.modcrafters.minecraft.bedandbranch.block;

import net.modcrafters.minecraft.bedandbranch.soil.SoilKind;
import net.modcrafters.minecraft.bedandbranch.soil.SoilState;
import net.modcrafters.minecraft.bedandbranch.soil.SoilLookup;
import net.modcrafters.minecraft.bedandbranch.soil.SoilRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class SoilFarmlandBlock extends FarmlandBlock {
    public SoilFarmlandBlock(Properties properties) {
        super(Blocks.DIRT, properties);
        registerDefaultState(defaultBlockState().setValue(SoilState.PROPERTY, SoilState.LOAM));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        SoilKind kind = SoilLookup.kindOf(context.getLevel().getBlockState(context.getClickedPos().below())).orElse(null);
        if (kind == null) {
            return null;
        }
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }
        return state.setValue(SoilState.PROPERTY, SoilState.of(kind));
    }

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
        if (collapses(level, pos)) {
            ticks.scheduleTick(pos, this, 1);
            return state;
        }
        SoilKind kind = SoilLookup.kindOf(level.getBlockState(pos.below())).orElseThrow();
        return super.updateShape(
                state.setValue(SoilState.PROPERTY, SoilState.of(kind)),
                level,
                ticks,
                pos,
                direction,
                neighborPos,
                neighborState,
                random
        );
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (collapses(level, pos)) {
            Block.popResource(level, pos, new ItemStack(Items.DIRT));
            level.removeBlock(pos, false);
            return;
        }
        super.tick(state, level, pos, random);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return !collapses(level, pos) && super.canSurvive(state, level, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SoilState.PROPERTY);
    }

    private static boolean collapses(LevelReader level, BlockPos pos) {
        return SoilRules.farmlandCollapses(SoilLookup.kindOf(level.getBlockState(pos.below())).isPresent());
    }
}
