package net.modcrafters.minecraft.bedandbranch.worldgen;

import net.modcrafters.minecraft.bedandbranch.block.CropStickBlock;
import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.modcrafters.minecraft.bedandbranch.block.StickPlant;
import net.modcrafters.minecraft.bedandbranch.soil.SoilState;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
//? if >=26.3 {
import com.mojang.serialization.MapCodec;
//?} else {
/*import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
*///?}

/** Fits an entire footprint before touching the world. Failed candidates leave no partial ruins. */
//? if >=26.3 {
public final class AbandonedFarmFeature implements Feature {
    private final MapCodec<AbandonedFarmFeature> codec = MapCodec.unit(this);
//?} else {
/*public final class AbandonedFarmFeature extends Feature<NoneFeatureConfiguration> {
*///?}
    private final FarmLayout layout;

    public AbandonedFarmFeature(FarmLayout layout) {
        //? if <26.3
        //super(NoneFeatureConfiguration.CODEC);
        this.layout = layout;
    }

    //? if >=26.3 {
    @Override
    public MapCodec<AbandonedFarmFeature> codec() { return codec; }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        return placeFarm(level, random, origin);
    }
    //?} else {
    /*@Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        return placeFarm(context.level(), context.random(), context.origin());
    }
    *///?}

    private boolean placeFarm(WorldGenLevel level, RandomSource random, BlockPos origin) {
        int firstRotation = random.nextInt(4);
        for (int turn = 0; turn < 4; turn++) {
            int rotation = (firstRotation + turn) & 3;
            int[][] heights = sample(level, origin, rotation);
            var fit = FarmTerrain.fit(layout, heights);
            if (fit.isEmpty()) continue;
            int base = fit.getAsInt();
            if (!clearAndSupported(level, origin, rotation, base, heights)) continue;
            build(level, origin, random, rotation, base, heights);
            return true;
        }
        return false;
    }

    private int[][] sample(WorldGenLevel level, BlockPos origin, int rotation) {
        int[][] heights = new int[layout.depth()][layout.width()];
        for (int z = 0; z < layout.depth(); z++) {
            for (int x = 0; x < layout.width(); x++) {
                BlockPos p = at(origin, x, z, rotation, 0);
                heights[z][x] = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p.getX(), p.getZ()) - 1;
            }
        }
        return heights;
    }

    private boolean clearAndSupported(WorldGenLevel level, BlockPos origin, int rotation, int base, int[][] heights) {
        // A one-block halo prevents a leveled bed ending abruptly over a cliff or inside a hillside.
        for (int z = -1; z <= layout.depth(); z++) {
            for (int x = -1; x <= layout.width(); x++) {
                if (x >= 0 && x < layout.width() && z >= 0 && z < layout.depth()) continue;
                int cx = Math.clamp(x, 0, layout.width() - 1);
                int cz = Math.clamp(z, 0, layout.depth() - 1);
                BlockPos p = at(origin, x, z, rotation, 0);
                int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p.getX(), p.getZ()) - 1;
                int expected = base + layout.elevation(cx, cz);
                if (Math.abs(ground - expected) > (layout.creek(cx, cz) ? 2 : 1)) return false;
            }
        }
        for (int z = 0; z < layout.depth(); z++) {
            for (int x = 0; x < layout.width(); x++) {
                int target = base + layout.elevation(x, z);
                BlockPos surface = at(origin, x, z, rotation, target);
                int original = heights[z][x];
                if (layout.creek(x, z)) {
                    BlockPos water = surface.below();
                    if (!level.getFluidState(water).is(FluidTags.WATER)
                            || !naturalGround(level.getBlockState(water.below()))
                            || !level.getBlockState(surface).isAir()
                            || !level.getBlockState(surface.above()).isAir()
                            || !level.ensureCanWrite(surface)) return false;
                    continue;
                }
                // Require real, natural ground under the base; never plug a deep hole or bury a build.
                for (int y = Math.min(original, target - 2); y <= original; y++) {
                    BlockPos p = new BlockPos(surface.getX(), y, surface.getZ());
                    if (!naturalGround(level.getBlockState(p)) || level.getBlockEntity(p) != null
                            || !level.ensureCanWrite(p)) return false;
                }
                for (int y = original + 1; y <= Math.max(original, target) + 3; y++) {
                    BlockPos p = new BlockPos(surface.getX(), y, surface.getZ());
                    if (!clearable(level.getBlockState(p)) || level.getBlockEntity(p) != null
                            || !level.ensureCanWrite(p)) return false;
                }
            }
        }
        return true;
    }

    private static boolean naturalGround(BlockState state) {
        return state.getFluidState().isEmpty() && (state.is(BlockTags.DIRT) || state.is(Blocks.MUD)
                || state.is(Blocks.CLAY) || state.is(Blocks.GRAVEL) || state.is(Blocks.SAND)
                || state.is(Blocks.STONE) || state.is(Blocks.ANDESITE) || state.is(Blocks.DIORITE)
                || state.is(Blocks.GRANITE));
    }

    private static boolean clearable(BlockState state) {
        return state.isAir() || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN)
                || state.is(BlockTags.FLOWERS);
    }

    private void build(WorldGenLevel level, BlockPos origin, RandomSource random, int rotation, int base, int[][] heights) {
        // Foundations first, then plants. Flooded farmland stores its water internally, with mud below.
        for (int z = 0; z < layout.depth(); z++) {
            for (int x = 0; x < layout.width(); x++) {
                char cell = layout.cell(x, z);
                BlockPos p = at(origin, x, z, rotation, base + layout.elevation(x, z));
                if (cell == 'W') continue;
                if (cell == 'B') {
                    put(level, p, Blocks.OAK_SLAB.defaultBlockState());
                    continue;
                }
                for (int y = heights[z][x] + 1; y < p.getY(); y++)
                    put(level, new BlockPos(p.getX(), y, p.getZ()), Blocks.DIRT.defaultBlockState());
                for (int y = p.getY() + 1; y <= Math.max(heights[z][x], p.getY()) + 3; y++)
                    put(level, new BlockPos(p.getX(), y, p.getZ()), Blocks.AIR.defaultBlockState());
                if (cell == 'I') {
                    put(level, p.below(), Blocks.MUD.defaultBlockState());
                    put(level, p, Blocks.WATER.defaultBlockState());
                } else if (cell == 'R' || cell == 'E' || cell == 'M') {
                    put(level, p.below(), Blocks.MUD.defaultBlockState());
                    put(level, p, ModBlocks.FARMLAND.defaultBlockState()
                            .setValue(SoilState.PROPERTY, SoilState.FLOODED_MUD).setValue(FarmlandBlock.MOISTURE, 7));
                } else {
                    BlockState ground = switch (cell) {
                        case 'L' -> ModBlocks.LOAM.defaultBlockState();
                        case 'P' -> Blocks.DIRT_PATH.defaultBlockState();
                        case 'C', 'O' -> Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                        default -> Blocks.GRASS_BLOCK.defaultBlockState();
                    };
                    put(level, p, ground);
                }
            }
        }
        for (int z = 0; z < layout.depth(); z++) {
            for (int x = 0; x < layout.width(); x++) {
                char cell = layout.cell(x, z);
                BlockPos p = at(origin, x, z, rotation, base + layout.elevation(x, z) + 1);
                switch (cell) {
                    case 'R' -> put(level, p, ModBlocks.CROP_STICK.defaultBlockState()
                            .setValue(StickPlant.PROPERTY, StickPlant.RICE).setValue(CropStickBlock.AGE, 2 + random.nextInt(6)));
                    case 'E' -> put(level, p, ModBlocks.CROP_STICK.defaultBlockState());
                    case 'F' -> put(level, p, Blocks.OAK_FENCE.defaultBlockState());
                    case 'C' -> {
                        // The terrace retaining course is at ground level; shed walls remain one block high.
                        if (layout != FarmLayout.TERRACED_PATCH) put(level, p, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
                    }
                    case 'O' -> {
                        put(level, p, Blocks.OAK_LOG.defaultBlockState());
                        if (random.nextBoolean()) put(level, p.above(), Blocks.OAK_LOG.defaultBlockState());
                    }
                    case '.' -> {
                        if (random.nextInt(3) == 0) put(level, p, Blocks.SHORT_GRASS.defaultBlockState());
                    }
                    default -> { }
                }
            }
        }
        // Recompute fence connections after every fence exists; worldgen uses no neighbor notifications.
        for (int z = 0; z < layout.depth(); z++) {
            for (int x = 0; x < layout.width(); x++) {
                if (layout.cell(x, z) != 'F') continue;
                BlockPos p = at(origin, x, z, rotation, base + layout.elevation(x, z) + 1);
                BlockState fence = Blocks.OAK_FENCE.defaultBlockState();
                for (var direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                    var property = switch (direction) {
                        case NORTH -> net.minecraft.world.level.block.state.properties.BlockStateProperties.NORTH;
                        case SOUTH -> net.minecraft.world.level.block.state.properties.BlockStateProperties.SOUTH;
                        case EAST -> net.minecraft.world.level.block.state.properties.BlockStateProperties.EAST;
                        default -> net.minecraft.world.level.block.state.properties.BlockStateProperties.WEST;
                    };
                    fence = fence.setValue(property, level.getBlockState(p.relative(direction)).is(Blocks.OAK_FENCE));
                }
                put(level, p, fence);
            }
        }
    }

    private BlockPos at(BlockPos origin, int x, int z, int rotation, int y) {
        return new BlockPos(origin.getX() + layout.worldX(x, z, rotation), y, origin.getZ() + layout.worldZ(x, z, rotation));
    }

    private static void put(WorldGenLevel level, BlockPos p, BlockState state) {
        level.setBlock(p, state, 2);
    }
}
