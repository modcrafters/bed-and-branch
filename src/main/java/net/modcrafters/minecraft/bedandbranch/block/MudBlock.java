package net.modcrafters.minecraft.bedandbranch.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
//? if <1.21.6
//import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class MudBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public MudBlock(Properties properties) {
        //? if >=1.21.2 {
        super(properties.overrideDescription("block.bed_and_branch.mud.by_state"));
        //?} else
        //super(properties);
        registerDefaultState(stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        return defaultBlockState().setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    //? if >=1.21.6 {
    @Override
    public boolean canPlaceLiquid(LivingEntity entity, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return acceptsWater(state, fluid);
    }
    //?} else {
    /*@Override
    public boolean canPlaceLiquid(Player player, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return acceptsWater(state, fluid);
    }
    *///?}

    private static boolean acceptsWater(BlockState state, Fluid fluid) {
        return fluid == Fluids.WATER && !state.getValue(WATERLOGGED);
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        if (state.getValue(WATERLOGGED) || fluidState.getType() != Fluids.WATER) {
            return false;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(WATERLOGGED, true), Block.UPDATE_ALL);
        }
        return true;
    }

    //? if >=1.21.6 {
    @Override
    public ItemStack pickupBlock(LivingEntity entity, LevelAccessor level, BlockPos pos, BlockState state) {
        return takeWater(level, pos, state);
    }
    //?} else {
    /*@Override
    public ItemStack pickupBlock(Player player, LevelAccessor level, BlockPos pos, BlockState state) {
        return takeWater(level, pos, state);
    }
    *///?}

    private ItemStack takeWater(LevelAccessor level, BlockPos pos, BlockState state) {
        if (!state.getValue(WATERLOGGED)) {
            return ItemStack.EMPTY;
        }
        level.setBlock(pos, state.setValue(WATERLOGGED, false), Block.UPDATE_ALL);
        return new ItemStack(Items.WATER_BUCKET);
    }

    //? if >=1.21.5 {
    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return namedClone(state);
    }
    //?} else {
    /*@Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return namedClone(state);
    }
    *///?}

    private ItemStack namedClone(BlockState state) {
        ItemStack stack = new ItemStack(this);
        String name = state.getValue(WATERLOGGED) ? "block.bed_and_branch.mud.flooded" : "block.bed_and_branch.mud.dry";
        stack.set(DataComponents.ITEM_NAME, Component.translatable(name));
        return stack;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }
}
