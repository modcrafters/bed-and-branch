package net.modcrafters.minecraft.bedandbranch.growth;

import net.modcrafters.minecraft.bedandbranch.soil.SoilKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpreadRulesTest {
    @Test
    void loamPlantsRefuseOtherSoils() {
        for (String plant : new String[]{"wheat", "beetroot", "pumpkin", "torchflower", "tomato", "sweet_berry", "corn"}) {
            assertTrue(SpreadRules.accepts(plant, SoilKind.LOAM), plant);
            assertFalse(SpreadRules.accepts(plant, SoilKind.SANDY), plant);
            assertFalse(SpreadRules.accepts(plant, SoilKind.PEAT), plant);
            assertFalse(SpreadRules.accepts(plant, SoilKind.MUD), plant);
            assertFalse(SpreadRules.accepts(plant, SoilKind.FLOODED_MUD), plant);
        }
    }

    @Test
    void sandyPlantsAlsoTakeLoam() {
        for (String plant : new String[]{"carrot", "potato", "melon"}) {
            assertTrue(SpreadRules.accepts(plant, SoilKind.SANDY), plant);
            assertTrue(SpreadRules.accepts(plant, SoilKind.LOAM), plant);
            assertFalse(SpreadRules.accepts(plant, SoilKind.PEAT), plant);
            assertFalse(SpreadRules.accepts(plant, SoilKind.MUD), plant);
            assertFalse(SpreadRules.accepts(plant, SoilKind.FLOODED_MUD), plant);
        }
    }

    @Test
    void riceSpreadsOnlyOnFloodedMud() {
        assertTrue(SpreadRules.accepts("rice", SoilKind.FLOODED_MUD));
        assertFalse(SpreadRules.accepts("rice", SoilKind.MUD));
        assertFalse(SpreadRules.accepts("rice", SoilKind.LOAM));
        assertFalse(SpreadRules.accepts("rice", SoilKind.SANDY));
        assertFalse(SpreadRules.accepts("rice", SoilKind.PEAT));
    }

    @Test
    void unknownPlantsAndMissingSoilDoNotSpread() {
        assertFalse(SpreadRules.accepts("onion", SoilKind.LOAM));
        assertFalse(SpreadRules.accepts("none", SoilKind.LOAM));
        assertFalse(SpreadRules.accepts(null, SoilKind.LOAM));
        assertFalse(SpreadRules.accepts("wheat", null));
    }

    @Test
    void aTryNeedsBothSoilsAndThenOneRollInFive() {
        assertFalse(SpreadRules.isAttempt(false, true));
        assertFalse(SpreadRules.isAttempt(true, false));
        assertFalse(SpreadRules.isAttempt(false, false));
        assertTrue(SpreadRules.isAttempt(true, true));
        assertTrue(SpreadRules.plants(0));
        assertFalse(SpreadRules.plants(1));
        assertFalse(SpreadRules.plants(4));
    }
}
