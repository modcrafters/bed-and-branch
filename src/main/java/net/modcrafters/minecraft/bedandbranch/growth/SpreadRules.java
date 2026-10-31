package net.modcrafters.minecraft.bedandbranch.growth;

import net.modcrafters.minecraft.bedandbranch.soil.SoilKind;

/**
 * Preferred soil for copying a mature plant onto an empty support.
 * A cross can later take this same attempt. Until then, a suitable support copies the parent.
 */
public final class SpreadRules {
    private SpreadRules() {
    }

    public static boolean accepts(String plant, SoilKind soil) {
        if (plant == null || soil == null) {
            return false;
        }
        return switch (plant) {
            case "wheat", "beetroot", "pumpkin", "torchflower", "tomato", "sweet_berry", "corn" -> soil == SoilKind.LOAM;
            case "carrot", "potato", "melon" -> soil == SoilKind.SANDY || soil == SoilKind.LOAM;
            case "rice" -> soil == SoilKind.FLOODED_MUD;
            default -> false;
        };
    }

    /** A failed soil check is not one of the five tries. No empty support is not a try either. */
    public static boolean isAttempt(boolean sourceSuits, boolean targetSuits) {
        return sourceSuits && targetSuits;
    }

    /** The roll is 0 through 4. Only 0 plants. */
    public static boolean plants(int roll) {
        return roll == 0;
    }
}
