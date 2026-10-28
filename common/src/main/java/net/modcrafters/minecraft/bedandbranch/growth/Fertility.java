package net.modcrafters.minecraft.bedandbranch.growth;

public final class Fertility {
    public static final int BASE = 1;
    public static final int WATER_BONUS = 3;

    private Fertility() {
    }

    public static int waterBonus(boolean watered) {
        return watered ? WATER_BONUS : 0;
    }

    public static int score(boolean watered) {
        return BASE + waterBonus(watered);
    }

    public static float growthSpeed(boolean watered) {
        return score(watered);
    }
}
