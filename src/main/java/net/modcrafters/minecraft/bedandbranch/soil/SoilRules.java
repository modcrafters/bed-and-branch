package net.modcrafters.minecraft.bedandbranch.soil;

import java.util.Optional;

// Built-in ids shipped in the soil tags. The game itself asks the tags, so a datapack can add a base.
public final class SoilRules {
    public static final String DIRT = "minecraft:dirt";

    private SoilRules() {
    }

    public static Optional<SoilKind> kindOf(String blockId) {
        return switch (blockId) {
            case "minecraft:sand", "minecraft:red_sand" -> Optional.of(SoilKind.SANDY);
            case "bed_and_branch:loam" -> Optional.of(SoilKind.LOAM);
            case "bed_and_branch:peat" -> Optional.of(SoilKind.PEAT);
            case "minecraft:mud", "bed_and_branch:mud" -> Optional.of(SoilKind.MUD);
            default -> Optional.empty();
        };
    }

    public static SoilKind withWater(SoilKind kind, boolean waterlogged) {
        if (kind == SoilKind.MUD && waterlogged) {
            return SoilKind.FLOODED_MUD;
        }
        return kind;
    }

    public static boolean shouldTill(String targetId, String baseId) {
        return DIRT.equals(targetId) && kindOf(baseId).isPresent();
    }

    public static boolean farmlandCollapses(boolean baseIsSoil) {
        return !baseIsSoil;
    }

    public static boolean tomatoAccepts(SoilKind base) {
        return base == SoilKind.LOAM;
    }

    public static boolean tomatoSurvives(boolean onOurFarmland, SoilKind base) {
        return onOurFarmland && base != null && tomatoAccepts(base);
    }

    /** Wild tomatoes take any soil under the tilled block. Stick tomatoes still want loam. */
    public static boolean wildTomatoSurvives(boolean onOurFarmland, SoilKind base) {
        return onOurFarmland && base != null;
    }
}
