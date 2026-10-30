package net.modcrafters.minecraft.bedandbranch.item;

import net.modcrafters.minecraft.bedandbranch.block.GardenSupportBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GardenSupportItem extends BlockItem {
    public GardenSupportItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos hit = clickedSupport(context);
        if (hit == null || !(getBlock() instanceof GardenSupportBlock support)) {
            return context;
        }
        BlockPos.MutableBlockPos top = hit.mutable();
        int height = 1;
        while (height < support.maxStack() && level.getBlockState(top.above()).is(support)) {
            top.move(Direction.UP);
            height++;
        }
        if (height >= support.maxStack()) {
            return context;
        }
        BlockPos above = top.above();
        if (!level.getBlockState(above).canBeReplaced(context)) {
            return context;
        }
        return BlockPlaceContext.at(context, above, Direction.UP);
    }

    /** The support that was clicked, whichever face the cursor was on. */
    private BlockPos clickedSupport(BlockPlaceContext context) {
        BlockPos placed = context.getClickedPos();
        BlockPos hit = context.replacingClickedOnBlock()
                ? placed
                : placed.relative(context.getClickedFace().getOpposite());
        Level level = context.getLevel();
        if (level.getBlockState(hit).is(getBlock())) {
            return hit;
        }
        BlockState atPlacement = level.getBlockState(placed);
        return atPlacement.is(getBlock()) ? placed : null;
    }
}
