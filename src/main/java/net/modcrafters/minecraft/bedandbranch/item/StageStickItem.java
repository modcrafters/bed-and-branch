package net.modcrafters.minecraft.bedandbranch.item;

import net.modcrafters.minecraft.bedandbranch.block.CropStickBlock;
import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.modcrafters.minecraft.bedandbranch.block.StickPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class StageStickItem extends Item {
    private final int delta;

    public StageStickItem(int delta, Properties properties) {
        super(properties);
        this.delta = delta;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (ageOf(state) == null && context.getClickedFace() == Direction.UP) {
            BlockPos above = pos.above();
            BlockState aboveState = level.getBlockState(above);
            if (ageOf(aboveState) != null) {
                pos = above;
                state = aboveState;
            }
        }
        IntegerProperty age = ageOf(state);
        if (age == null) {
            return InteractionResult.PASS;
        }
        int next = state.getValue(age) + delta;
        if (!age.getPossibleValues().contains(next)) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(age, next), Block.UPDATE_CLIENTS);
        }
        return InteractionResult.SUCCESS;
    }

    private static IntegerProperty ageOf(BlockState state) {
        if (state.is(ModBlocks.CROP_STICK) && state.getValue(StickPlant.PROPERTY) == StickPlant.NONE) {
            return null;
        }
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty age && property.getName().equals(CropStickBlock.AGE.getName())) {
                return age;
            }
        }
        return null;
    }
}
