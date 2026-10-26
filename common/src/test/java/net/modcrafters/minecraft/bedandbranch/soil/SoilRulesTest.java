package net.modcrafters.minecraft.bedandbranch.soil;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoilRulesTest {
    @Test
    void knownBasesMapToTheirSoil() {
        assertEquals(Optional.of(SoilKind.SANDY), SoilRules.kindOf("minecraft:sand"));
        assertEquals(Optional.of(SoilKind.SANDY), SoilRules.kindOf("minecraft:red_sand"));
        assertEquals(Optional.of(SoilKind.LOAM), SoilRules.kindOf("bed_and_branch:loam"));
        assertEquals(Optional.of(SoilKind.PEAT), SoilRules.kindOf("bed_and_branch:peat"));
        assertEquals(Optional.of(SoilKind.FLOODED_MUD), SoilRules.kindOf("bed_and_branch:flooded_mud"));
    }

    @Test
    void otherBlocksAreNotSoilBases() {
        assertEquals(Optional.empty(), SoilRules.kindOf("minecraft:dirt"));
        assertEquals(Optional.empty(), SoilRules.kindOf("minecraft:mud"));
        assertEquals(Optional.empty(), SoilRules.kindOf("minecraft:farmland"));
        assertEquals(Optional.empty(), SoilRules.kindOf("minecraft:stone"));
    }

    @Test
    void hoeConvertsOnlyDirtSittingOnASoilBase() {
        assertTrue(SoilRules.shouldTill("minecraft:dirt", "minecraft:sand"));
        assertTrue(SoilRules.shouldTill("minecraft:dirt", "minecraft:red_sand"));
        assertTrue(SoilRules.shouldTill("minecraft:dirt", "bed_and_branch:loam"));
        assertTrue(SoilRules.shouldTill("minecraft:dirt", "bed_and_branch:peat"));
        assertTrue(SoilRules.shouldTill("minecraft:dirt", "bed_and_branch:flooded_mud"));
        assertFalse(SoilRules.shouldTill("minecraft:dirt", "minecraft:stone"));
        assertFalse(SoilRules.shouldTill("minecraft:dirt", "minecraft:dirt"));
        assertFalse(SoilRules.shouldTill("minecraft:grass_block", "bed_and_branch:loam"));
        assertFalse(SoilRules.shouldTill("minecraft:coarse_dirt", "minecraft:sand"));
        assertFalse(SoilRules.shouldTill("minecraft:farmland", "bed_and_branch:loam"));
    }

    @Test
    void farmlandCollapsesWhenTheBaseIsGone() {
        assertTrue(SoilRules.farmlandCollapses(false));
        assertFalse(SoilRules.farmlandCollapses(true));
    }

    @Test
    void tomatoAcceptsOnlyLoam() {
        assertTrue(SoilRules.tomatoAccepts(SoilKind.LOAM));
        assertFalse(SoilRules.tomatoAccepts(SoilKind.SANDY));
        assertFalse(SoilRules.tomatoAccepts(SoilKind.PEAT));
        assertFalse(SoilRules.tomatoAccepts(SoilKind.FLOODED_MUD));
    }

    @Test
    void tomatoNeedsOurFarmlandAndLoam() {
        assertTrue(SoilRules.tomatoSurvives(true, SoilKind.LOAM));
        assertFalse(SoilRules.tomatoSurvives(false, SoilKind.LOAM));
        assertFalse(SoilRules.tomatoSurvives(true, SoilKind.SANDY));
        assertFalse(SoilRules.tomatoSurvives(true, null));
    }

    @Test
    void shippedTagsMatchTheBuiltInIds() throws IOException {
        assertTag("soils/sandy", SoilKind.SANDY, "minecraft:sand", "minecraft:red_sand");
        assertTag("soils/loam", SoilKind.LOAM, "bed_and_branch:loam");
        assertTag("soils/peat", SoilKind.PEAT, "bed_and_branch:peat");
        assertTag("soils/flooded_mud", SoilKind.FLOODED_MUD, "bed_and_branch:flooded_mud");
        String bases = readTag("soil_bases");
        assertTrue(bases.contains("#bed_and_branch:soils/sandy"));
        assertTrue(bases.contains("#bed_and_branch:soils/loam"));
        assertTrue(bases.contains("#bed_and_branch:soils/peat"));
        assertTrue(bases.contains("#bed_and_branch:soils/flooded_mud"));
    }

    private static void assertTag(String path, SoilKind kind, String... ids) throws IOException {
        String json = readTag(path);
        for (String id : ids) {
            assertTrue(json.contains("\"" + id + "\""), id);
            assertEquals(Optional.of(kind), SoilRules.kindOf(id));
        }
    }

    private static String readTag(String path) throws IOException {
        String resource = "/data/bed_and_branch/tags/block/" + path + ".json";
        try (InputStream in = SoilRulesTest.class.getResourceAsStream(resource)) {
            assertTrue(in != null, resource);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
