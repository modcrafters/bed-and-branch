package net.modcrafters.minecraft.bedandbranch.item;

import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.modcrafters.minecraft.bedandbranch.block.SoilFarmlandBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
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
            return SeedPlanting.tryPlant(level, context.getPlayer(), context.getItemInHand(), pos);
        }
        if (state.getBlock() instanceof SoilFarmlandBlock && context.getClickedFace() == Direction.UP) {
            return SeedPlanting.tryPlant(level, context.getPlayer(), context.getItemInHand(), pos.above());
        }
        return InteractionResult.PASS;
    }
}
