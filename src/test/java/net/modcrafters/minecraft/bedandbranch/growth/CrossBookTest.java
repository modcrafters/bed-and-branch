package net.modcrafters.minecraft.bedandbranch.growth;

import net.modcrafters.minecraft.bedandbranch.soil.SoilKind;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CrossBookTest {
    @Test
    void wheatAndRiceOnLoamMakeCorn() {
        assertEquals("corn", CrossBook.pick("minecraft:wheat", List.of("bed_and_branch:rice"), SoilKind.LOAM, bound -> 0));
        assertEquals("corn", CrossBook.pick("bed_and_branch:rice", List.of("minecraft:wheat"), SoilKind.LOAM, bound -> 0));
    }

    @Test
    void theEmptyStickHasToSuitCorn() {
        assertNull(CrossBook.pick("minecraft:wheat", List.of("bed_and_branch:rice"), SoilKind.FLOODED_MUD, bound -> 0));
        assertNull(CrossBook.pick("minecraft:wheat", List.of("bed_and_branch:rice"), SoilKind.SANDY, bound -> 0));
        assertNull(CrossBook.pick("minecraft:wheat", List.of("bed_and_branch:rice"), SoilKind.MUD, bound -> 0));
    }

    @Test
    void oneParentDoesNotCross() {
        assertNull(CrossBook.pick("minecraft:wheat", List.of(), SoilKind.LOAM, bound -> 0));
        assertNull(CrossBook.pick("minecraft:wheat", List.of("minecraft:beetroot"), SoilKind.LOAM, bound -> 0));
    }

    @Test
    void theBuiltinFileIsThatCross() {
        String json = """
                {
                  "parents": ["bed_and_branch:rice", "minecraft:wheat"],
                  "offspring": "bed_and_branch:corn_seeds"
                }
                """;
        CrossBook.Recipe recipe = CrossBook.parse(json, "test");
        assertEquals("corn", recipe.offspring());
        assertEquals(1, recipe.weight());
    }
}
