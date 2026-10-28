package net.modcrafters.minecraft.bedandbranch.look;

import net.modcrafters.minecraft.bedandbranch.growth.Fertility;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlantLookTest {
    @Test
    void waterRaisesFertilityByThree() {
        assertEquals(1, Fertility.score(false));
        assertEquals(0, Fertility.waterBonus(false));
        assertEquals(4, Fertility.score(true));
        assertEquals(3, Fertility.waterBonus(true));
        assertEquals(4.0F, Fertility.growthSpeed(true));
    }

    @Test
    void wateredTomatoShowsTheImprovement() {
        assertEquals(List.of(
                "Stage 2/7",
                "Soil: loam",
                "Suits this plant",
                "Fertility 4 (water +3)",
                "Staked"
        ), PlantLookText.lines(new PlantLook("loam", 4, 3, "2/7", PlantLook.Suitability.SUITS, true)));
    }

    @Test
    void dryCropOnTheWrongSoilNamesWhatItNeeds() {
        assertEquals(List.of(
                "Stage 0/7",
                "Soil: sandy",
                "Needs loam",
                "Fertility 1"
        ), PlantLookText.lines(new PlantLook("sandy", 1, 0, "0/7", PlantLook.Suitability.NEEDS_LOAM, false)));
    }

    @Test
    void bareBedShowsSoilAndFertility() {
        assertEquals(List.of(
                "Soil: peat",
                "Fertility 4 (water +3)"
        ), PlantLookText.lines(new PlantLook("peat", 4, 3, null, PlantLook.Suitability.UNSPECIFIED, false)));
    }
}
