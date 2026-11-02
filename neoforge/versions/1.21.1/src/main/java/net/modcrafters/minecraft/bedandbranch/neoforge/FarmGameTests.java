package net.modcrafters.minecraft.bedandbranch.neoforge;

import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.modcrafters.minecraft.bedandbranch.block.StickPlant;
import net.modcrafters.minecraft.bedandbranch.soil.SoilState;
import net.modcrafters.minecraft.bedandbranch.worldgen.FarmLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Runtime placement regressions, discovered only by the NeoForge game-test runner. */
@GameTestHolder("bed_and_branch")
@PrefixGameTestTemplate(false)
public final class FarmGameTests {
    @GameTest(template = "farm_test_space", timeoutTicks = 80)
    public static void pocket(GameTestHelper helper) { place(helper, FarmLayout.POCKET_PADDY); }
    @GameTest(template = "farm_test_space", timeoutTicks = 80)
    public static void split(GameTestHelper helper) { place(helper, FarmLayout.SPLIT_BEDS); }
    @GameTest(template = "farm_test_space", timeoutTicks = 80)
    public static void creek(GameTestHelper helper) { place(helper, FarmLayout.CREEKSIDE_PLOT); }
    @GameTest(template = "farm_test_space", timeoutTicks = 80)
    public static void terrace(GameTestHelper helper) { place(helper, FarmLayout.TERRACED_PATCH); }
    @GameTest(template = "farm_test_space", timeoutTicks = 80)
    public static void ruin(GameTestHelper helper) { place(helper, FarmLayout.RUINED_FARMSTEAD); }

    private static void prepare(GameTestHelper helper, FarmLayout layout) {
        for (int z = 0; z < 25; z++) for (int x = 0; x < 25; x++) {
            int height = 4 + (layout == FarmLayout.TERRACED_PATCH && z < 12 ? 1 : 0);
            // Open the test harness's barrier roof so surface heightmaps see the actual terrain.
            for (int y = 0; y < 15; y++) {
                BlockState state = y < height ? Blocks.DIRT.defaultBlockState()
                        : y == height ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.AIR.defaultBlockState();
                if (layout == FarmLayout.CREEKSIDE_PLOT && x == 8) {
                    if (y == 4) state = Blocks.AIR.defaultBlockState();
                    if (y == 3) state = Blocks.WATER.defaultBlockState();
                    if (y == 2) state = Blocks.MUD.defaultBlockState();
                }
                helper.getLevel().setBlock(helper.absolutePos(new BlockPos(x,y,z)), state, 2);
            }
        }
    }

    private static boolean generate(GameTestHelper helper, FarmLayout layout) {
        var level = helper.getLevel();
        var configured = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE)
                .get(ResourceLocation.fromNamespaceAndPath("bed_and_branch", layout.id()));
        helper.assertTrue(configured != null, "Configured farm feature is registered");
        return configured.place(level, level.getChunkSource().getGenerator(), RandomSource.create(47),
                helper.absolutePos(new BlockPos(12,5,12)));
    }

    private static void place(GameTestHelper helper, FarmLayout layout) {
        prepare(helper, layout);
        helper.assertTrue(generate(helper, layout), "Farm should fit its intended terrain: " + layout.id());
        helper.runAfterDelay(10, () -> {
            int rice = 0;
            for (int z = 6; z < 19; z++) for (int x = 6; x < 19; x++) for (int y = 4; y < 8; y++) {
                BlockPos p = helper.absolutePos(new BlockPos(x,y,z));
                BlockState state = helper.getLevel().getBlockState(p);
                if (state.is(ModBlocks.CROP_STICK) && state.getValue(StickPlant.PROPERTY) == StickPlant.RICE) {
                    rice++;
                    BlockState bed = helper.getLevel().getBlockState(p.below());
                    helper.assertTrue(bed.is(ModBlocks.FARMLAND) && bed.getValue(SoilState.PROPERTY) == SoilState.FLOODED_MUD,
                            "Rice remains on flooded farmland");
                    helper.assertTrue(helper.getLevel().getBlockState(p.below(2)).is(Blocks.MUD), "Mud supports the bed");
                }
            }
            helper.assertTrue(rice >= 3, "Surviving rice provides a starter supply");
            helper.succeed();
        });
    }

    @GameTest(template = "farm_test_space", timeoutTicks = 80)
    public static void rejectWithoutPartialPlacement(GameTestHelper helper) {
        prepare(helper, FarmLayout.POCKET_PADDY);
        helper.setBlock(12,5,12, Blocks.CHEST);
        var before = new java.util.ArrayList<BlockState>();
        for (int z = 8; z <= 16; z++) for (int x = 8; x <= 16; x++) for (int y = 2; y <= 8; y++)
            before.add(helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(x,y,z))));
        helper.assertFalse(generate(helper, FarmLayout.POCKET_PADDY), "A chest must block placement in every orientation");
        int index = 0;
        for (int z = 8; z <= 16; z++) for (int x = 8; x <= 16; x++) for (int y = 2; y <= 8; y++)
            helper.assertTrue(before.get(index++).equals(helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(x,y,z)))),
                    "Rejected farm must leave every block untouched");
        helper.setBlock(12,5,12, Blocks.AIR);
        helper.assertFalse(generate(helper, FarmLayout.TERRACED_PATCH), "Terraces must not spawn on flat land");
        helper.succeed();
    }
}
