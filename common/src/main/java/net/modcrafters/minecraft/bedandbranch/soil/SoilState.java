package net.modcrafters.minecraft.bedandbranch.soil;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public enum SoilState implements StringRepresentable {
    SANDY(SoilKind.SANDY),
    LOAM(SoilKind.LOAM),
    PEAT(SoilKind.PEAT),
    FLOODED_MUD(SoilKind.FLOODED_MUD);

    public static final EnumProperty<SoilState> PROPERTY = EnumProperty.create("soil", SoilState.class);

    private final SoilKind kind;

    SoilState(SoilKind kind) {
        this.kind = kind;
    }

    public SoilKind kind() {
        return kind;
    }

    public static SoilState of(SoilKind kind) {
        for (SoilState state : values()) {
            if (state.kind == kind) {
                return state;
            }
        }
        throw new IllegalArgumentException(kind.name());
    }

    @Override
    public String getSerializedName() {
        return kind.serializedName();
    }
}
