package net.modcrafters.minecraft.bedandbranch.command;

import net.modcrafters.minecraft.bedandbranch.soil.SoilKind;

import java.util.Optional;

public final class BedInfoText {
    private BedInfoText() {
    }

    public static String format(Optional<SoilKind> base, boolean farmland, boolean stick) {
        String soil = base.map(SoilKind::label).orElse("none");
        return "Base: " + soil + ". Farmland: " + yesNo(farmland) + ". Stick: " + yesNo(stick) + ".";
    }

    private static String yesNo(boolean value) {
        return value ? "yes" : "no";
    }
}
