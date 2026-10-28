package net.modcrafters.minecraft.bedandbranch.item;

import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.modcrafters.minecraft.bedandbranch.block.TomatoCropBlock;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CropStickItem extends BlockItem {
    public CropStickItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        if (state.is(ModBlocks.TOMATO) && !state.getValue(TomatoCropBlock.STAKED)) {
            if (!level.isClientSide()) {
                level.setBlock(context.getClickedPos(), state.setValue(TomatoCropBlock.STAKED, true), Block.UPDATE_ALL);
                if (context.getPlayer() == null || !context.getPlayer().hasInfiniteMaterials()) {
                    context.getItemInHand().shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }
}
