package net.modcrafters.minecraft.bedandbranch.soil;

import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public final class SoilLookup {
    private SoilLookup() {
    }

    public static Optional<SoilKind> kindOf(BlockState state) {
        if (state.is(ModTags.SOILS_SANDY)) {
            return Optional.of(SoilKind.SANDY);
        }
        if (state.is(ModTags.SOILS_LOAM)) {
            return Optional.of(SoilKind.LOAM);
        }
        if (state.is(ModTags.SOILS_PEAT)) {
            return Optional.of(SoilKind.PEAT);
        }
        if (state.is(ModTags.SOILS_FLOODED_MUD)) {
            return Optional.of(SoilKind.FLOODED_MUD);
        }
        return Optional.empty();
    }
}
