package net.modcrafters.minecraft.bedandbranch.block;

import net.modcrafters.minecraft.bedandbranch.item.StageStickItem;
//? if <1.21.2
//import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
//? if <1.21.2
//import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class VineTrellisBlock extends GardenSupportBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** 0 is a bare trellis. 1 to 4 is a vanilla vine climbing the panel. */
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 4);
    /** Kept so trellises placed while this counted a ready cycle still load. It no longer delays spreading. */
    public static final IntegerProperty SPREAD = IntegerProperty.create("spread", 0, 4);
    public static final EnumProperty<TrellisVine> VINE = EnumProperty.create("vine", TrellisVine.class);
    public static final int MAX_AGE = 4;

    // The panel is one pixel north of center. Turning the model moves that pixel, so each facing has its own box.
    private static final VoxelShape NORTH = Block.box(0.0, -1.0, 7.0, 16.0, 15.0, 8.0);
    private static final VoxelShape EAST = Block.box(8.0, -1.0, 0.0, 9.0, 15.0, 16.0);
    private static final VoxelShape SOUTH = Block.box(0.0, -1.0, 8.0, 16.0, 15.0, 9.0);
    private static final VoxelShape WEST = Block.box(7.0, -1.0, 0.0, 8.0, 15.0, 16.0);

    public VineTrellisBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(AGE, 0)
                .setValue(SPREAD, 0)
                .setValue(VINE, TrellisVine.VINE));
    }

    @Override
    public int maxStack() {
        return 3;
    }

    // BushBlock requires a codec through 1.21.1. VegetationBlock supplies it from 1.21.5.
    //? if >=1.21.2 {
    //?} else {
    /*public static final MapCodec<VineTrellisBlock> CODEC = simpleCodec(VineTrellisBlock::new);

    @Override
    public MapCodec<VineTrellisBlock> codec() {
        return CODEC;
    }
    *///?}

    @Override
    protected BlockState orient(BlockState state, BlockPlaceContext context) {
        BlockState below = context.getLevel().getBlockState(context.getClickedPos().below());
        Direction facing = below.is(this) ? below.getValue(FACING) : context.getHorizontalDirection();
        return state.setValue(FACING, facing);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> NORTH;
        };
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, params));
        if (state.getValue(AGE) > 0) {
            drops.add(new ItemStack(vineItem(state.getValue(VINE))));
        }
        return drops;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        TrellisVine vine = state.getValue(VINE);
        if (age == 0 || !canGrow(level, pos, vine) || random.nextInt(4) != 0) {
            return;
        }
        if (age < MAX_AGE) {
            level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
            return;
        }
        climb(level, pos, vine);
        if (!isGround(level, pos)) {
            return;
        }
        List<BlockPos> open = openGroundNeighbors(level, pos, vine);
        if (open.isEmpty() || random.nextInt(5) != 0) {
            return;
        }
        BlockPos target = open.get(random.nextInt(open.size()));
        BlockState neighbor = level.getBlockState(target);
        level.setBlock(target, neighbor.setValue(AGE, 1).setValue(SPREAD, 0).setValue(VINE, vine), Block.UPDATE_CLIENTS);
    }

    /** Overworld vines want daylight. Weeping vines want fertilized nether farmland, and the nether is dark. */
    private boolean canGrow(ServerLevel level, BlockPos pos, TrellisVine vine) {
        if (vine == TrellisVine.WEEPING) {
            return fertilized(level, pos);
        }
        return level.getRawBrightness(pos, 0) >= 9;
    }

    private boolean fertilized(Level level, BlockPos pos) {
        BlockState soil = level.getBlockState(bottomOf(level, pos).below());
        return soil.getBlock() instanceof NetherFarmlandBlock && soil.getValue(FarmlandBlock.MOISTURE) > 0;
    }

    /** Grow the first unfinished trellis above this one, so a stack fills to the top. */
    private void climb(ServerLevel level, BlockPos pos, TrellisVine vine) {
        BlockPos cursor = pos.above();
        while (true) {
            BlockState above = level.getBlockState(cursor);
            if (!above.is(this)) {
                return;
            }
            int age = above.getValue(AGE);
            if (age < MAX_AGE) {
                level.setBlock(cursor, grown(above, age == 0 ? 1 : age + 1, vine), Block.UPDATE_CLIENTS);
                return;
            }
            cursor = cursor.above();
        }
    }

    /**
     * The +1 stick, on a finished vine, starts the empty trellis directly above.
     * Returns whether that trellis was there and empty.
     */
    public static boolean startAbove(Level level, BlockPos pos, BlockState state) {
        if (!state.is(ModBlocks.VINE_TRELLIS) || state.getValue(AGE) < MAX_AGE) {
            return false;
        }
        BlockState above = level.getBlockState(pos.above());
        if (!above.is(ModBlocks.VINE_TRELLIS) || above.getValue(AGE) != 0) {
            return false;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos.above(), grown(above, 1, state.getValue(VINE)), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    private static BlockState grown(BlockState state, int age, TrellisVine vine) {
        return state.setValue(AGE, age).setValue(SPREAD, 0).setValue(VINE, vine);
    }

    /**
     * Debug stick. A full ground vine copies itself onto one empty trellis now, skipping the 1-in-5.
     * An upper panel counts as the vine below it.
     */
    public static boolean spreadNow(Level level, BlockPos pos, BlockState state) {
        if (!state.is(ModBlocks.VINE_TRELLIS) || state.getValue(AGE) == 0) {
            return false;
        }
        if (!(level.getBlockState(pos).getBlock() instanceof VineTrellisBlock trellis)) {
            return false;
        }
        BlockPos root = trellis.bottomOf(level, pos);
        BlockState rootState = level.getBlockState(root);
        if (!rootState.is(ModBlocks.VINE_TRELLIS) || rootState.getValue(AGE) < MAX_AGE || !trellis.isGround(level, root)) {
            return false;
        }
        TrellisVine vine = rootState.getValue(VINE);
        List<BlockPos> open = trellis.openGroundNeighbors(level, root, vine);
        if (open.isEmpty()) {
            return false;
        }
        if (!level.isClientSide()) {
            BlockPos target = open.getFirst();
            BlockState neighbor = level.getBlockState(target);
            level.setBlock(target, neighbor.setValue(AGE, 1).setValue(SPREAD, 0).setValue(VINE, vine), Block.UPDATE_CLIENTS);
            level.setBlock(root, rootState.setValue(SPREAD, MAX_AGE), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    private List<BlockPos> openGroundNeighbors(Level level, BlockPos pos, TrellisVine vine) {
        List<BlockPos> open = new ArrayList<>();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(direction);
            BlockState state = level.getBlockState(neighbor);
            if (state.is(this) && state.getValue(AGE) == 0 && accepts(level, neighbor, vine)) {
                open.add(neighbor);
            }
        }
        return open;
    }

    private boolean accepts(Level level, BlockPos pos, TrellisVine vine) {
        BlockState soil = level.getBlockState(pos.below());
        if (vine == TrellisVine.WEEPING) {
            return soil.getBlock() instanceof NetherFarmlandBlock;
        }
        return soil.getBlock() instanceof FarmlandBlock && !(soil.getBlock() instanceof NetherFarmlandBlock);
    }

    private boolean isGround(Level level, BlockPos pos) {
        return level.getBlockState(pos.below()).getBlock() instanceof FarmlandBlock;
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
        InteractionResult handled = useTrellis(stack, state, level, pos, player);
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
        InteractionResult handled = useTrellis(stack, state, level, pos, player);
        if (handled != null) {
            return net.modcrafters.minecraft.bedandbranch.item.SeedPlanting.asItem(handled);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
    *///?}

    private InteractionResult useTrellis(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player) {
        InteractionResult planted = plantVine(stack, level, pos, player);
        if (planted != null) {
            return planted;
        }
        if (stack.is(asItem()) || stack.getItem() instanceof StageStickItem) {
            return null;
        }
        if (StageStickItem.heldBy(player)) {
            return InteractionResult.CONSUME;
        }
        return harvest(level, pos, state);
    }

    private InteractionResult harvest(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(AGE) < MAX_AGE) {
            return null;
        }
        if (!level.isClientSide()) {
            int picked = 0;
            BlockPos cursor = pos;
            while (true) {
                BlockState current = level.getBlockState(cursor);
                if (!current.is(this)) {
                    break;
                }
                if (current.getValue(AGE) > 0) {
                    picked++;
                    int age = isGround(level, cursor) ? 1 : 0;
                    TrellisVine vine = age == 0 ? TrellisVine.NONE : current.getValue(VINE);
                    level.setBlock(cursor, current.setValue(AGE, age).setValue(SPREAD, 0).setValue(VINE, vine), Block.UPDATE_ALL);
                }
                cursor = cursor.above();
            }
            Item drop = vineItem(state.getValue(VINE));
            while (picked > 0) {
                int give = Math.min(picked, drop.getDefaultMaxStackSize());
                Block.popResource(level, pos, new ItemStack(drop, give));
                picked -= give;
            }
            level.playSound(null, pos, SoundEvents.VINE_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult plantVine(ItemStack stack, Level level, BlockPos pos, Player player) {
        if (!stack.is(Items.VINE) && !stack.is(Items.WEEPING_VINES)) {
            return null;
        }
        BlockPos root = bottomOf(level, pos);
        BlockState rootState = level.getBlockState(root);
        if (!rootState.is(this) || rootState.getValue(AGE) != 0) {
            return null;
        }
        TrellisVine vine = vineFor(stack, level.getBlockState(root.below()));
        if (vine == null) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            level.setBlock(root, rootState.setValue(AGE, 1).setValue(VINE, vine), Block.UPDATE_ALL);
            if (!player.hasInfiniteMaterials()) {
                stack.shrink(1);
            }
            level.playSound(null, root, SoundEvents.VINE_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    private static TrellisVine vineFor(ItemStack stack, BlockState soil) {
        boolean nether = soil.getBlock() instanceof NetherFarmlandBlock;
        if (stack.is(Items.WEEPING_VINES) && nether) {
            return TrellisVine.WEEPING;
        }
        if (stack.is(Items.VINE) && soil.getBlock() instanceof FarmlandBlock && !nether) {
            return TrellisVine.VINE;
        }
        return null;
    }

    private static Item vineItem(TrellisVine vine) {
        return vine == TrellisVine.WEEPING ? Items.WEEPING_VINES : Items.VINE;
    }

    private BlockPos bottomOf(Level level, BlockPos pos) {
        BlockPos cursor = pos;
        while (level.getBlockState(cursor.below()).is(this)) {
            cursor = cursor.below();
        }
        return cursor;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AGE, SPREAD, VINE);
    }

    public enum TrellisVine implements StringRepresentable {
        NONE("none"),
        VINE("vine"),
        WEEPING("weeping");

        private final String name;

        TrellisVine(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
