package net.modcrafters.minecraft.bedandbranch.block;

import net.modcrafters.minecraft.bedandbranch.item.ModItems;
import net.modcrafters.minecraft.bedandbranch.soil.SoilLookup;
import net.modcrafters.minecraft.bedandbranch.soil.SoilRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class TomatoCropBlock extends CropBlock {
    public static final BooleanProperty STAKED = BooleanProperty.create("staked");

    public TomatoCropBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(STAKED, false));
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getBlock() instanceof FarmlandBlock;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!super.canSurvive(state, level, pos)) {
            return false;
        }
        // Vanilla farmland has no soil base. Our tilled soil still needs one underneath.
        BlockState below = level.getBlockState(pos.below());
        if (!(below.getBlock() instanceof SoilFarmlandBlock)) {
            return true;
        }
        return SoilRules.wildTomatoSurvives(true, SoilLookup.kindOf(level.getBlockState(pos.below(2))).orElse(null));
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.TOMATO_SEEDS;
    }

    // The crosshair uses the picked stack's name. Seeds stay tomato seeds; the wild plant shows its own name.
    //? if >=1.21.5 {
    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return namedClone(state, super.getCloneItemStack(level, pos, state, includeData));
    }
    //?} else {
    /*@Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return namedClone(state, super.getCloneItemStack(level, pos, state));
    }
    *///?}

    private static ItemStack namedClone(BlockState state, ItemStack stack) {
        if (!state.getValue(STAKED)) {
            stack.set(DataComponents.ITEM_NAME, Component.translatable("block.bed_and_branch.tomato"));
        }
        return stack;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STAKED);
    }
}
