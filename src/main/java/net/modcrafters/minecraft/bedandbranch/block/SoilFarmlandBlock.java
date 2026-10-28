package net.modcrafters.minecraft.bedandbranch.block;

import net.modcrafters.minecraft.bedandbranch.soil.SoilKind;
import net.modcrafters.minecraft.bedandbranch.soil.SoilState;
import net.modcrafters.minecraft.bedandbranch.soil.SoilLookup;
import net.modcrafters.minecraft.bedandbranch.soil.SoilRules;
import net.modcrafters.minecraft.bedandbranch.item.SeedPlanting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
//? if <1.21.2
//import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.LevelReader;
//? if >=1.21.2
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class SoilFarmlandBlock extends FarmlandBlock {
    public SoilFarmlandBlock(Properties properties) {
        // Farmland names the dirt block it collapses back into since 26.1.
        //? if >=26.1 {
        super(Blocks.DIRT, properties);
        //?} else
        //super(properties);
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
        if (collapses(level, pos)) {
            ticks.scheduleTick(pos, this, 1);
            return state;
        }
        return super.updateShape(withSoil(state, level, pos), level, ticks, pos, direction, neighborPos, neighborState, random);
    }
    //?} else {
    /*@Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (collapses(level, pos)) {
            level.scheduleTick(pos, this, 1);
            return state;
        }
        return super.updateShape(withSoil(state, level, pos), direction, neighborState, level, pos, neighborPos);
    }
    *///?}

    private BlockState withSoil(BlockState state, LevelReader level, BlockPos pos) {
        SoilKind kind = SoilLookup.kindOf(level.getBlockState(pos.below())).orElseThrow();
        return state.setValue(SoilState.PROPERTY, SoilState.of(kind));
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

    //? if >=1.21.2 {
    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        InteractionResult handled = useSoil(stack, level, pos, player);
        if (handled != null) {
            return handled;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
    //?} else {
    /*@Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        InteractionResult handled = useSoil(stack, level, pos, player);
        if (handled != null) {
            return SeedPlanting.asItem(handled);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
    *///?}

    private InteractionResult useSoil(ItemStack stack, Level level, BlockPos pos, Player player) {
        BlockPos above = pos.above();
        BlockState crop = level.getBlockState(above);
        if (!crop.is(ModBlocks.CROP_STICK)) {
            return null;
        }
        InteractionResult planted = SeedPlanting.tryPlant(level, player, stack, above);
        if (planted.consumesAction()) {
            return planted;
        }
        InteractionResult harvested = CropStickBlock.harvest(level, above, crop);
        if (harvested.consumesAction()) {
            return harvested;
        }
        return null;
    }

    @Override
    public MutableComponent getName() {
        return Component.translatable("item.bed_and_branch.farmland");
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
        stack.set(DataComponents.ITEM_NAME, Component.translatable(state.getValue(SoilState.PROPERTY).kind().farmlandNameKey()));
        return stack;
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
