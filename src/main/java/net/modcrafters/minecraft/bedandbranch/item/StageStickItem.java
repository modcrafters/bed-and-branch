package net.modcrafters.minecraft.bedandbranch.item;

import net.modcrafters.minecraft.bedandbranch.block.BerryObeliskBlock;
import net.modcrafters.minecraft.bedandbranch.block.CropStickBlock;
import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.modcrafters.minecraft.bedandbranch.block.StickPlant;
import net.modcrafters.minecraft.bedandbranch.block.VineTrellisBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
        int current = state.getValue(age);
        if (delta > 0 && current >= finalStage(state, age)) {
            if (VineTrellisBlock.startAbove(level, pos, state) || BerryObeliskBlock.startAbove(level, pos, state)) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.CONSUME;
        }
        int next = current + delta;
        if (!age.getPossibleValues().contains(next)) {
            return InteractionResult.CONSUME;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(age, next), Block.UPDATE_CLIENTS);
        }
        return InteractionResult.SUCCESS;
    }

    /** A stage stick is in one of the hands, so this click must not harvest. */
    public static boolean heldBy(Player player) {
        if (player == null) {
            return false;
        }
        return player.getMainHandItem().getItem() instanceof StageStickItem
                || player.getOffhandItem().getItem() instanceof StageStickItem;
    }

    private static IntegerProperty ageOf(BlockState state) {
        if (state.is(ModBlocks.CROP_STICK) && state.getValue(StickPlant.PROPERTY) == StickPlant.NONE) {
            return null;
        }
        if (state.is(ModBlocks.VINE_TRELLIS) && state.getValue(VineTrellisBlock.AGE) == 0) {
            return null;
        }
        if (state.is(ModBlocks.BERRY_OBELISK) && state.getValue(BerryObeliskBlock.AGE) == 0) {
            return null;
        }
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty age && property.getName().equals(CropStickBlock.AGE.getName())) {
                return age;
            }
        }
        return null;
    }

    /** The stage a plant can be harvested at. The +1 stick stops there. */
    private static int finalStage(BlockState state, IntegerProperty age) {
        if (state.is(ModBlocks.CROP_STICK)) {
            return state.getValue(StickPlant.PROPERTY).maxAge();
        }
        if (state.is(ModBlocks.VINE_TRELLIS)) {
            return VineTrellisBlock.MAX_AGE;
        }
        int limit = 0;
        for (int value : age.getPossibleValues()) {
            limit = Math.max(limit, value);
        }
        return limit;
    }
}
