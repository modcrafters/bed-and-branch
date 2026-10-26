package net.modcrafters.minecraft.bedandbranch.soil;

public enum SoilKind {
    SANDY("sandy", "sandy"),
    LOAM("loam", "loam"),
    PEAT("peat", "peat"),
    FLOODED_MUD("flooded_mud", "flooded mud");

    private final String serialized;
    private final String label;

    SoilKind(String serialized, String label) {
        this.serialized = serialized;
        this.label = label;
    }

    public String serializedName() {
        return serialized;
    }

    public String label() {
        return label;
    }
}
