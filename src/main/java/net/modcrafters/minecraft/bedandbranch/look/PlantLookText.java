package net.modcrafters.minecraft.bedandbranch.look;

import java.util.ArrayList;
import java.util.List;

public final class PlantLookText {
    private PlantLookText() {
    }

    public static List<String> lines(PlantLook look) {
        List<String> lines = new ArrayList<>();
        if (look.stage() != null) {
            lines.add("Stage " + look.stage());
        }
        lines.add("Soil: " + look.soil());
        if (look.suitability() == PlantLook.Suitability.SUITS) {
            lines.add("Suits this plant");
        } else if (look.suitability() == PlantLook.Suitability.NEEDS_LOAM) {
            lines.add("Needs loam");
        }
        if (look.waterBonus() > 0) {
            lines.add("Fertility " + look.fertility() + " (water +" + look.waterBonus() + ")");
        } else {
            lines.add("Fertility " + look.fertility());
        }
        if (look.staked()) {
            lines.add("Staked");
        }
        return lines;
    }
}
