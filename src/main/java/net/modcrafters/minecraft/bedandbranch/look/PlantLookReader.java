package net.modcrafters.minecraft.bedandbranch.look;

import net.modcrafters.minecraft.bedandbranch.block.CropStickBlock;
import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.modcrafters.minecraft.bedandbranch.block.SoilFarmlandBlock;
import net.modcrafters.minecraft.bedandbranch.block.StickPlant;
import net.modcrafters.minecraft.bedandbranch.block.TomatoCropBlock;
import net.modcrafters.minecraft.bedandbranch.growth.Fertility;
import net.modcrafters.minecraft.bedandbranch.soil.SoilKind;
import net.modcrafters.minecraft.bedandbranch.soil.SoilLookup;
import net.modcrafters.minecraft.bedandbranch.soil.SoilState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class PlantLookReader {
    private PlantLookReader() {
    }

    public static PlantLook read(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof SoilFarmlandBlock) {
            return fromBed(level, pos, state, level.getBlockState(pos.above()));
        }
        BlockState below = level.getBlockState(pos.below());
        if (!(below.getBlock() instanceof SoilFarmlandBlock)) {
            return null;
        }
        if (!isPlant(state)) {
            return null;
        }
        return fromBed(level, pos.below(), below, state);
    }

    private static PlantLook fromBed(LevelReader level, BlockPos farmlandPos, BlockState farmland, BlockState plant) {
        SoilKind soil = SoilLookup.kindOf(level.getBlockState(farmlandPos.below()))
                .orElseGet(() -> farmland.getValue(SoilState.PROPERTY).kind());
        boolean watered = farmland.hasProperty(FarmlandBlock.MOISTURE) && farmland.getValue(FarmlandBlock.MOISTURE) > 0;
        Growing growing = growing(plant);
        PlantLook.Suitability suitability = PlantLook.Suitability.UNSPECIFIED;
        if (growing.tomato()) {
            suitability = soil == SoilKind.LOAM ? PlantLook.Suitability.SUITS : PlantLook.Suitability.NEEDS_LOAM;
        }
        return new PlantLook(
                soil.label(),
                Fertility.score(watered),
                Fertility.waterBonus(watered),
                growing.stage(),
                suitability,
                growing.staked()
        );
    }

    private static boolean isPlant(BlockState state) {
        return state.is(ModBlocks.CROP_STICK)
                || state.getBlock() instanceof CropBlock
                || state.getBlock() instanceof StemBlock;
    }

    private static Growing growing(BlockState state) {
        if (state.is(ModBlocks.CROP_STICK)) {
            StickPlant plant = state.getValue(StickPlant.PROPERTY);
            if (plant == StickPlant.NONE) {
                return Growing.NONE;
            }
            return new Growing(stage(state.getValue(CropStickBlock.AGE), plant.maxAge()), plant == StickPlant.TOMATO, false);
        }
        if (state.getBlock() instanceof TomatoCropBlock tomato) {
            return new Growing(
                    stage(tomato.getAge(state), tomato.getMaxAge()),
                    true,
                    state.getValue(TomatoCropBlock.STAKED)
            );
        }
        if (state.getBlock() instanceof CropBlock crop) {
            return new Growing(stage(crop.getAge(state), crop.getMaxAge()), false, false);
        }
        if (state.getBlock() instanceof StemBlock) {
            return new Growing(stage(state.getValue(StemBlock.AGE), StemBlock.MAX_AGE), false, false);
        }
        return Growing.NONE;
    }

    private static String stage(int age, int maxAge) {
        return age + "/" + maxAge;
    }

    private record Growing(String stage, boolean tomato, boolean staked) {
        private static final Growing NONE = new Growing(null, false, false);
    }
}
