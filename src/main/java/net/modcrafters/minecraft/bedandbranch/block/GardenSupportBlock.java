package net.modcrafters.minecraft.bedandbranch.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FarmlandBlock;
//? if >=1.21.5
import net.minecraft.world.level.block.VegetationBlock;
//? if <1.21.5
//import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;

// One block tall, with the feet one pixel into the block below. A stack starts on farmland.
//? if >=1.21.5 {
public abstract class GardenSupportBlock extends VegetationBlock {
//?} else {
/*public abstract class GardenSupportBlock extends BushBlock {
*///?}
    protected GardenSupportBlock(Properties properties) {
        super(properties);
    }

    public abstract int maxStack();

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getBlock() instanceof FarmlandBlock || state.is(this);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null || stackBelow(context) >= maxStack()) {
            return null;
        }
        return orient(state, context);
    }

    /** How many of this support are already stacked under the spot being placed. */
    private int stackBelow(BlockPlaceContext context) {
        BlockGetter level = context.getLevel();
        BlockPos pos = context.getClickedPos().below();
        int count = 0;
        while (count < maxStack() && level.getBlockState(pos).is(this)) {
            count++;
            pos = pos.below();
        }
        return count;
    }

    protected BlockState orient(BlockState state, BlockPlaceContext context) {
        return state;
    }
}
