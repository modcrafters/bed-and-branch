package net.modcrafters.minecraft.bedandbranch.block;

import net.modcrafters.minecraft.bedandbranch.growth.CrossBook;
import net.modcrafters.minecraft.bedandbranch.growth.Fertility;
import net.modcrafters.minecraft.bedandbranch.growth.SpreadRules;
import net.modcrafters.minecraft.bedandbranch.item.SeedPlanting;
import net.modcrafters.minecraft.bedandbranch.item.StageStickItem;
import net.modcrafters.minecraft.bedandbranch.soil.SoilKind;
//? if <1.21.2
//import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
//? if <1.21.2
//import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
//? if >=1.21.5
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.FarmlandBlock;
//? if >=1.21.5
import net.minecraft.world.level.block.VegetationBlock;
//? if <1.21.5
//import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

//? if >=1.21.5 {
public class CropStickBlock extends VegetationBlock implements BonemealableBlock {
//?} else {
/*public class CropStickBlock extends BushBlock implements BonemealableBlock {
*///?}
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 7);
    /** Kept so sticks placed while this counted a ready cycle still load. It no longer delays spreading. */
    public static final IntegerProperty SPREAD = IntegerProperty.create("spread", 0, 7);

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1.0, -1.0, 1.0, 2.0, 15.0, 2.0),
            Block.box(14.0, -1.0, 1.0, 15.0, 15.0, 2.0),
            Block.box(1.0, -1.0, 14.0, 2.0, 15.0, 15.0),
            Block.box(14.0, -1.0, 14.0, 15.0, 15.0, 15.0)
    );

    public CropStickBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(StickPlant.PROPERTY, StickPlant.NONE)
                .setValue(AGE, 0)
                .setValue(SPREAD, 0));
    }

    /** Open torchflowers shine. Younger stages and every other plant stay dark. */
    public static int lightEmission(BlockState state) {
        if (state.getValue(StickPlant.PROPERTY) != StickPlant.TORCHFLOWER) {
            return 0;
        }
        return state.getValue(AGE) >= StickPlant.TORCHFLOWER.maxAge() ? 12 : 0;
    }

    // BushBlock requires a codec through 1.21.1. VegetationBlock supplies it from 1.21.5.
    //? if >=1.21.2 {
    //?} else {
    /*public static final MapCodec<CropStickBlock> CODEC = simpleCodec(CropStickBlock::new);

    @Override
    public MapCodec<CropStickBlock> codec() {
        return CODEC;
    }
    *///?}

    public static boolean sitsOn(BlockState state) {
        return state.getBlock() instanceof FarmlandBlock;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return sitsOn(state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, params));
        state.getValue(StickPlant.PROPERTY).addBreakDrops(drops, state.getValue(AGE));
        return drops;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        StickPlant plant = state.getValue(StickPlant.PROPERTY);
        if (plant == StickPlant.NONE) {
            return;
        }
        if (!plant.survives(level, pos)) {
            Block.popResource(level, pos, new ItemStack(plant.seed()));
            level.setBlock(pos, state.setValue(StickPlant.PROPERTY, StickPlant.NONE).setValue(AGE, 0).setValue(SPREAD, 0), Block.UPDATE_ALL);
            return;
        }
        int age = state.getValue(AGE);
        if (level.getRawBrightness(pos, 0) < 9) {
            return;
        }
        // Beets and torchflowers skip one growth roll in three, the same as their vanilla crops.
        if ((plant == StickPlant.BEETROOT || plant == StickPlant.TORCHFLOWER) && random.nextInt(3) == 0) {
            return;
        }
        float speed = growthSpeed(level, pos);
        if (random.nextInt((int) (25.0F / speed) + 1) != 0) {
            return;
        }
        if (age >= plant.maxAge()) {
            spread(level, pos, state, plant, random);
            return;
        }
        level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
    }

    /**
     * A mature plant picks one empty stick beside it.
     * That stick looks at the mature plants beside it. A cross that suits the empty soil wins.
     * Otherwise the plant copies itself when the empty soil suits it too.
     * Neither is not a try, so it does not spend the 1 in 5.
     */
    private static void spread(ServerLevel level, BlockPos pos, BlockState state, StickPlant plant, RandomSource random) {
        if (!SpreadRules.accepts(plant.getSerializedName(), farmlandSoil(level, pos))) {
            return;
        }
        CrossBook.use(level.getServer().getResourceManager());
        List<BlockPos> empty = new ArrayList<>();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(direction);
            BlockState there = level.getBlockState(neighbor);
            if (there.is(ModBlocks.CROP_STICK) && there.getValue(StickPlant.PROPERTY) == StickPlant.NONE) {
                empty.add(neighbor);
            }
        }
        if (empty.isEmpty()) {
            return;
        }
        BlockPos target = empty.get(random.nextInt(empty.size()));
        StickPlant child = outcome(level, pos, target, plant, random);
        if (child == null || !SpreadRules.plants(random.nextInt(5))) {
            return;
        }
        plantChild(level, pos, state, target, child);
    }

    /**
     * Debug stick. A finished plant crosses or copies onto one empty stick now, skipping the 1-in-5.
     */
    public static boolean spreadNow(Level level, BlockPos pos, BlockState state) {
        if (!state.is(ModBlocks.CROP_STICK)) {
            return false;
        }
        StickPlant plant = state.getValue(StickPlant.PROPERTY);
        if (plant == StickPlant.NONE || state.getValue(AGE) < plant.maxAge()) {
            return false;
        }
        if (!SpreadRules.accepts(plant.getSerializedName(), farmlandSoil(level, pos))) {
            return false;
        }
        if (level instanceof ServerLevel server) {
            CrossBook.use(server.getServer().getResourceManager());
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos target = pos.relative(direction);
            BlockState there = level.getBlockState(target);
            if (!there.is(ModBlocks.CROP_STICK) || there.getValue(StickPlant.PROPERTY) != StickPlant.NONE) {
                continue;
            }
            StickPlant child = outcome(level, pos, target, plant, level instanceof ServerLevel server ? server.getRandom() : null);
            if (child == null) {
                continue;
            }
            if (level instanceof ServerLevel server) {
                plantChild(server, pos, state, target, child);
            }
            return true;
        }
        return false;
    }

    private static StickPlant outcome(Level level, BlockPos source, BlockPos target, StickPlant spreading, RandomSource random) {
        List<String> partners = new ArrayList<>();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos beside = target.relative(direction);
            if (beside.equals(source)) {
                continue;
            }
            BlockState there = level.getBlockState(beside);
            if (!there.is(ModBlocks.CROP_STICK)) {
                continue;
            }
            StickPlant neighbor = there.getValue(StickPlant.PROPERTY);
            if (neighbor == StickPlant.NONE || there.getValue(AGE) < neighbor.maxAge()) {
                continue;
            }
            if (!SpreadRules.accepts(neighbor.getSerializedName(), farmlandSoil(level, beside))) {
                continue;
            }
            partners.add(neighbor.plantId());
        }
        SoilKind soil = farmlandSoil(level, target);
        String crossed = CrossBook.pick(spreading.plantId(), partners, soil, bound -> random == null ? 0 : random.nextInt(bound));
        StickPlant child = StickPlant.byName(crossed);
        if (child != null && child.survives(level, target)) {
            return child;
        }
        if (SpreadRules.accepts(spreading.getSerializedName(), soil) && spreading.survives(level, target)) {
            return spreading;
        }
        return null;
    }

    private static void plantChild(ServerLevel level, BlockPos source, BlockState sourceState, BlockPos target, StickPlant child) {
        BlockState neighbor = level.getBlockState(target);
        level.setBlock(
                target,
                neighbor.setValue(StickPlant.PROPERTY, child).setValue(AGE, 0).setValue(SPREAD, 0),
                Block.UPDATE_CLIENTS
        );
        level.setBlock(source, sourceState.setValue(SPREAD, sourceState.getValue(StickPlant.PROPERTY).maxAge()), Block.UPDATE_CLIENTS);
        level.playSound(null, target, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private static SoilKind farmlandSoil(Level level, BlockPos support) {
        return SoilFarmlandBlock.kind(level.getBlockState(support.below()));
    }

    public static InteractionResult usePlantedStick(ItemStack stack, Level level, BlockPos pos, Player player) {
        BlockState crop = level.getBlockState(pos);
        if (!crop.is(ModBlocks.CROP_STICK)) {
            return null;
        }
        InteractionResult planted = SeedPlanting.tryPlant(level, player, stack, pos);
        if (planted.consumesAction()) {
            return planted;
        }
        if (stack.getItem() instanceof StageStickItem) {
            return null;
        }
        if (StageStickItem.heldBy(player)) {
            return InteractionResult.CONSUME;
        }
        InteractionResult harvested = harvest(level, pos, crop);
        if (harvested.consumesAction()) {
            return harvested;
        }
        return null;
    }

    public static InteractionResult harvest(Level level, BlockPos pos, BlockState state) {
        if (!state.is(ModBlocks.CROP_STICK)) {
            return InteractionResult.PASS;
        }
        StickPlant plant = state.getValue(StickPlant.PROPERTY);
        if (plant.fruit() == null || state.getValue(AGE) < plant.maxAge()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            Block.popResource(level, pos, new ItemStack(plant.fruit()));
            level.setBlock(pos, state.setValue(AGE, 0).setValue(SPREAD, 0), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
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
        InteractionResult handled = useStick(stack, state, level, pos, player);
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
        InteractionResult handled = useStick(stack, state, level, pos, player);
        if (handled != null) {
            return SeedPlanting.asItem(handled);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
    *///?}

    private InteractionResult useStick(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player) {
        InteractionResult planted = SeedPlanting.tryPlant(level, player, stack, pos);
        if (planted.consumesAction()) {
            return planted;
        }
        if (stack.getItem() instanceof StageStickItem) {
            return null;
        }
        if (StageStickItem.heldBy(player)) {
            return InteractionResult.CONSUME;
        }
        InteractionResult harvested = harvest(level, pos, state);
        if (harvested.consumesAction()) {
            return harvested;
        }
        return null;
    }

    //? if >=1.21.5 {
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return canGrow(state);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        grow(level, random, pos, state);
    }
    //?} else {
    /*@Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return canGrow(state);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        grow(level, random, pos, state);
    }
    *///?}

    private static boolean canGrow(BlockState state) {
        StickPlant plant = state.getValue(StickPlant.PROPERTY);
        return plant != StickPlant.NONE && state.getValue(AGE) < plant.maxAge();
    }

    private static void grow(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        StickPlant plant = state.getValue(StickPlant.PROPERTY);
        int age = Math.min(plant.maxAge(), state.getValue(AGE) + 2 + random.nextInt(4));
        level.setBlock(pos, state.setValue(AGE, age), Block.UPDATE_CLIENTS);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(StickPlant.PROPERTY, AGE, SPREAD);
    }

    private static float growthSpeed(BlockGetter level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return Fertility.growthSpeed(below.hasProperty(FarmlandBlock.MOISTURE) && below.getValue(FarmlandBlock.MOISTURE) > 0);
    }
}
