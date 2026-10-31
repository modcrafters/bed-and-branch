package net.modcrafters.minecraft.bedandbranch.block;

import net.modcrafters.minecraft.bedandbranch.growth.Fertility;
import net.modcrafters.minecraft.bedandbranch.growth.SpreadRules;
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
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class BerryObeliskBlock extends GardenSupportBlock implements BonemealableBlock {
    /** 0 is an empty cage. 1 to 4 is the branch structure. Berries are a separate layer on a full bush. */
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 4);
    public static final IntegerProperty FRUIT = IntegerProperty.create("fruit", 0, 2);
    /** Kept so bushes placed while this counted a ready cycle still load. It no longer delays spreading. */
    public static final IntegerProperty SPREAD = IntegerProperty.create("spread", 0, 4);
    /** The bottom cage holds the base of the bush. The stacked cage holds the top. */
    public static final EnumProperty<ObeliskPart> PART = EnumProperty.create("part", ObeliskPart.class);
    public static final int MAX_AGE = 4;
    public static final int FRUIT_MAX = 2;

    private static final VoxelShape SHAPE = rods();

    public BerryObeliskBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(AGE, 0)
                .setValue(FRUIT, 0)
                .setValue(PART, ObeliskPart.BASE)
                .setValue(SPREAD, 0));
    }

    @Override
    public int maxStack() {
        return 2;
    }

    // BushBlock requires a codec through 1.21.1. VegetationBlock supplies it from 1.21.5.
    //? if >=1.21.2 {
    //?} else {
    /*public static final MapCodec<BerryObeliskBlock> CODEC = simpleCodec(BerryObeliskBlock::new);

    @Override
    public MapCodec<BerryObeliskBlock> codec() {
        return CODEC;
    }
    *///?}

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, params));
        if (state.getValue(FRUIT) >= FRUIT_MAX) {
            drops.add(new ItemStack(Items.SWEET_BERRIES, 2));
        }
        return drops;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age == 0) {
            return;
        }
        if (level.getRawBrightness(pos, 0) < 9) {
            return;
        }
        float speed = Fertility.growthSpeed(watered(level, pos));
        if (random.nextInt((int) (25.0F / speed) + 1) != 0) {
            return;
        }
        if (age < MAX_AGE) {
            level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
            return;
        }
        growIntoAbove(level, pos);
        growFruit(level, pos, state);
        if (isGround(level, pos)) {
            spread(level, pos, random);
        }
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
        InteractionResult handled = useObelisk(stack, state, level, pos, player);
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
        InteractionResult handled = useObelisk(stack, state, level, pos, player);
        if (handled != null) {
            return net.modcrafters.minecraft.bedandbranch.item.SeedPlanting.asItem(handled);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
    *///?}

    private InteractionResult useObelisk(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player) {
        InteractionResult planted = plantBerry(stack, level, pos, player);
        if (planted != null) {
            return planted;
        }
        if (stack.is(asItem()) || stack.getItem() instanceof StageStickItem) {
            return null;
        }
        if (StageStickItem.heldBy(player)) {
            return InteractionResult.CONSUME;
        }
        InteractionResult harvested = harvest(level, pos, state);
        if (harvested != null) {
            return harvested;
        }
        if (stack.is(Items.SWEET_BERRIES)) {
            return InteractionResult.FAIL;
        }
        return null;
    }

    /** Sweet berries always go on the bottom cage. A second obelisk above stays empty. */
    private InteractionResult plantBerry(ItemStack stack, Level level, BlockPos pos, Player player) {
        if (!stack.is(Items.SWEET_BERRIES)) {
            return null;
        }
        BlockPos root = bottomOf(level, pos);
        BlockState rootState = level.getBlockState(root);
        if (!rootState.is(this) || rootState.getValue(AGE) != 0) {
            return null;
        }
        if (!accepts(level.getBlockState(root.below()))) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            level.setBlock(
                    root,
                    rootState.setValue(AGE, 1).setValue(FRUIT, 0).setValue(PART, ObeliskPart.BASE).setValue(SPREAD, 0),
                    Block.UPDATE_ALL
            );
            if (player == null || !player.hasInfiniteMaterials()) {
                stack.shrink(1);
            }
            level.playSound(null, root, SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult harvest(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(FRUIT) < FRUIT_MAX) {
            return null;
        }
        if (!level.isClientSide()) {
            int berries = 2;
            if (state.getValue(PART) == ObeliskPart.BASE) {
                BlockPos above = pos.above();
                BlockState upper = level.getBlockState(above);
                if (upper.is(this) && upper.getValue(FRUIT) >= FRUIT_MAX) {
                    berries += 2;
                    level.setBlock(above, upper.setValue(FRUIT, 0), Block.UPDATE_ALL);
                }
            }
            level.setBlock(pos, state.setValue(FRUIT, 0), Block.UPDATE_ALL);
            Block.popResource(level, pos, new ItemStack(Items.SWEET_BERRIES, berries));
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    //? if >=1.21.5 {
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
        return canSpreadUp(level, pos, state);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
        growOne(level, pos, state);
    }
    //?} else {
    /*@Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return canSpreadUp(level, pos, state);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        growOne(level, pos, state);
    }
    *///?}

    private static boolean canSpreadUp(LevelReader level, BlockPos pos, BlockState state) {
        if (state.getValue(AGE) <= 0) {
            return false;
        }
        if (state.getValue(AGE) < MAX_AGE || state.getValue(FRUIT) < FRUIT_MAX) {
            return true;
        }
        BlockState above = level.getBlockState(pos.above());
        return above.is(ModBlocks.BERRY_OBELISK) && above.getValue(AGE) < MAX_AGE;
    }

    private static void growOne(ServerLevel level, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age > 0 && age < MAX_AGE) {
            level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
            return;
        }
        if (age >= MAX_AGE) {
            growIntoAbove(level, pos);
            growFruit(level, pos, state);
        }
    }

    /** The +1 stick, on a full bush, starts the cage above or sets the next berries. */
    public static boolean startAbove(Level level, BlockPos pos, BlockState state) {
        if (!state.is(ModBlocks.BERRY_OBELISK) || state.getValue(AGE) < MAX_AGE) {
            return false;
        }
        BlockState above = level.getBlockState(pos.above());
        if (above.is(ModBlocks.BERRY_OBELISK) && above.getValue(AGE) == 0) {
            if (!level.isClientSide()) {
                level.setBlock(pos.above(), above.setValue(AGE, 1).setValue(FRUIT, 0).setValue(PART, ObeliskPart.TOP), Block.UPDATE_CLIENTS);
            }
            return true;
        }
        if (state.getValue(FRUIT) >= FRUIT_MAX) {
            return false;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(FRUIT, state.getValue(FRUIT) + 1), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    private static void growFruit(ServerLevel level, BlockPos pos, BlockState state) {
        int fruit = state.getValue(FRUIT);
        if (fruit >= FRUIT_MAX) {
            return;
        }
        level.setBlock(pos, state.setValue(FRUIT, fruit + 1), Block.UPDATE_CLIENTS);
    }

    /** @return whether the cage above took another stage of branches */
    private static boolean growIntoAbove(ServerLevel level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockState above = level.getBlockState(abovePos);
        if (!above.is(ModBlocks.BERRY_OBELISK)) {
            return false;
        }
        int age = above.getValue(AGE);
        if (age >= MAX_AGE) {
            return false;
        }
        level.setBlock(abovePos, above.setValue(AGE, age == 0 ? 1 : age + 1).setValue(PART, ObeliskPart.TOP), Block.UPDATE_CLIENTS);
        return true;
    }

    /**
     * A full ground bush copies itself onto an empty ground obelisk on loam, on the same rolls that grew it,
     * and only on one try in five. The cage above does not spread.
     */
    private static void spread(ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(ModBlocks.BERRY_OBELISK)) {
            return;
        }
        boolean sourceSuits = SpreadRules.accepts("sweet_berry", farmlandSoil(level, pos));
        List<BlockPos> empty = new ArrayList<>();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(direction);
            BlockState there = level.getBlockState(neighbor);
            if (there.is(ModBlocks.BERRY_OBELISK) && there.getValue(AGE) == 0 && isGround(level, neighbor)) {
                empty.add(neighbor);
            }
        }
        if (empty.isEmpty()) {
            return;
        }
        BlockPos target = empty.get(random.nextInt(empty.size()));
        boolean targetSuits = SpreadRules.accepts("sweet_berry", farmlandSoil(level, target));
        if (!SpreadRules.isAttempt(sourceSuits, targetSuits) || !SpreadRules.plants(random.nextInt(5))) {
            return;
        }
        BlockState neighbor = level.getBlockState(target);
        level.setBlock(
                target,
                neighbor.setValue(AGE, 1).setValue(FRUIT, 0).setValue(PART, ObeliskPart.BASE).setValue(SPREAD, 0),
                Block.UPDATE_CLIENTS
        );
        level.playSound(null, target, SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    /**
     * Debug stick. A full ground bush copies itself onto one empty obelisk now, skipping the 1-in-5.
     * The cage above counts as the bush below it.
     */
    public static boolean spreadNow(Level level, BlockPos pos, BlockState state) {
        if (!state.is(ModBlocks.BERRY_OBELISK) || state.getValue(AGE) == 0) {
            return false;
        }
        BlockPos root = bottomOf(level, pos);
        BlockState rootState = level.getBlockState(root);
        if (!rootState.is(ModBlocks.BERRY_OBELISK) || rootState.getValue(AGE) < MAX_AGE || !isGround(level, root)) {
            return false;
        }
        if (!SpreadRules.accepts("sweet_berry", farmlandSoil(level, root))) {
            return false;
        }
        BlockPos target = emptyObelisk(level, root);
        if (target == null) {
            return false;
        }
        if (!level.isClientSide()) {
            BlockState neighbor = level.getBlockState(target);
            level.setBlock(
                    target,
                    neighbor.setValue(AGE, 1).setValue(FRUIT, 0).setValue(PART, ObeliskPart.BASE).setValue(SPREAD, 0),
                    Block.UPDATE_CLIENTS
            );
            level.setBlock(root, rootState.setValue(SPREAD, MAX_AGE), Block.UPDATE_CLIENTS);
            level.playSound(null, target, SoundEvents.SWEET_BERRY_BUSH_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return true;
    }

    private static BlockPos emptyObelisk(Level level, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(direction);
            BlockState there = level.getBlockState(neighbor);
            if (!there.is(ModBlocks.BERRY_OBELISK) || there.getValue(AGE) != 0 || !isGround(level, neighbor)) {
                continue;
            }
            if (SpreadRules.accepts("sweet_berry", farmlandSoil(level, neighbor))) {
                return neighbor;
            }
        }
        return null;
    }

    private static SoilKind farmlandSoil(Level level, BlockPos support) {
        return SoilFarmlandBlock.kind(level.getBlockState(support.below()));
    }

    private static boolean isGround(Level level, BlockPos pos) {
        return accepts(level.getBlockState(pos.below()));
    }

    /** Hand planting is any ordinary farmland. Nether farmland is for weeping vines. */
    private static boolean accepts(BlockState soil) {
        return soil.getBlock() instanceof FarmlandBlock && !(soil.getBlock() instanceof NetherFarmlandBlock);
    }

    private static boolean watered(Level level, BlockPos pos) {
        BlockState soil = level.getBlockState(bottomOf(level, pos).below());
        return soil.hasProperty(FarmlandBlock.MOISTURE) && soil.getValue(FarmlandBlock.MOISTURE) > 0;
    }

    private static BlockPos bottomOf(Level level, BlockPos pos) {
        BlockPos cursor = pos;
        while (level.getBlockState(cursor.below()).is(ModBlocks.BERRY_OBELISK)) {
            cursor = cursor.below();
        }
        return cursor;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, FRUIT, PART, SPREAD);
    }

    /** Posts and the plank bands, the same boxes as the model. */
    private static VoxelShape rods() {
        VoxelShape shape = Shapes.empty();
        for (double x : new double[]{3.0, 12.0}) {
            for (double z : new double[]{3.0, 12.0}) {
                shape = Shapes.or(shape, Block.box(x, -1.0, z, x + 1.0, 15.0, z + 1.0));
            }
        }
        for (double y : new double[]{1.0, 8.0, 14.0}) {
            shape = Shapes.or(
                    shape,
                    Block.box(4.0, y, 3.0, 12.0, y + 1.0, 4.0),
                    Block.box(4.0, y, 12.0, 12.0, y + 1.0, 13.0),
                    Block.box(3.0, y, 4.0, 4.0, y + 1.0, 12.0),
                    Block.box(12.0, y, 4.0, 13.0, y + 1.0, 12.0)
            );
        }
        return shape;
    }

    public enum ObeliskPart implements StringRepresentable {
        BASE("base"),
        TOP("top");

        private final String name;

        ObeliskPart(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
