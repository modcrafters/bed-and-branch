package net.modcrafters.minecraft.bedandbranch.look;

public record PlantLook(
        String soil,
        int fertility,
        int waterBonus,
        String stage,
        Suitability suitability,
        boolean staked
) {
    public enum Suitability {
        UNSPECIFIED,
        SUITS,
        NEEDS_LOAM
    }
}
