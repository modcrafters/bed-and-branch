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
        assertEquals(Optional.of(SoilKind.MUD), SoilRules.kindOf("minecraft:mud"));
        assertEquals(Optional.of(SoilKind.MUD), SoilRules.kindOf("bed_and_branch:mud"));
    }

    @Test
    void waterTurnsMudIntoFloodedMud() {
        assertEquals(SoilKind.MUD, SoilRules.withWater(SoilKind.MUD, false));
        assertEquals(SoilKind.FLOODED_MUD, SoilRules.withWater(SoilKind.MUD, true));
        assertEquals(SoilKind.LOAM, SoilRules.withWater(SoilKind.LOAM, true));
    }

    @Test
    void otherBlocksAreNotSoilBases() {
        assertEquals(Optional.empty(), SoilRules.kindOf("minecraft:dirt"));
        assertEquals(Optional.empty(), SoilRules.kindOf("minecraft:farmland"));
        assertEquals(Optional.empty(), SoilRules.kindOf("minecraft:stone"));
    }

    @Test
    void hoeConvertsOnlyDirtSittingOnASoilBase() {
        assertTrue(SoilRules.shouldTill("minecraft:dirt", "minecraft:sand"));
        assertTrue(SoilRules.shouldTill("minecraft:dirt", "minecraft:red_sand"));
        assertTrue(SoilRules.shouldTill("minecraft:dirt", "bed_and_branch:loam"));
        assertTrue(SoilRules.shouldTill("minecraft:dirt", "bed_and_branch:peat"));
        assertTrue(SoilRules.shouldTill("minecraft:dirt", "minecraft:mud"));
        assertTrue(SoilRules.shouldTill("minecraft:dirt", "bed_and_branch:mud"));
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
    void farmlandNamesNameTheSoil() throws IOException {
        String lang = readResource("/assets/bed_and_branch/lang/en_us.json");
        assertNamed(lang, SoilKind.SANDY, "Sandy Tilled Soil");
        assertNamed(lang, SoilKind.LOAM, "Loam Tilled Soil");
        assertNamed(lang, SoilKind.PEAT, "Peat Tilled Soil");
        assertNamed(lang, SoilKind.MUD, "Muddy Tilled Soil");
        assertNamed(lang, SoilKind.FLOODED_MUD, "Flooded Mud Tilled Soil");
    }

    @Test
    void tomatoAcceptsOnlyLoam() {
        assertTrue(SoilRules.tomatoAccepts(SoilKind.LOAM));
        assertFalse(SoilRules.tomatoAccepts(SoilKind.SANDY));
        assertFalse(SoilRules.tomatoAccepts(SoilKind.PEAT));
        assertFalse(SoilRules.tomatoAccepts(SoilKind.MUD));
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
    void wildTomatoSurvivesOnAnySoil() {
        assertTrue(SoilRules.wildTomatoSurvives(true, SoilKind.LOAM));
        assertTrue(SoilRules.wildTomatoSurvives(true, SoilKind.SANDY));
        assertTrue(SoilRules.wildTomatoSurvives(true, SoilKind.PEAT));
        assertTrue(SoilRules.wildTomatoSurvives(true, SoilKind.MUD));
        assertTrue(SoilRules.wildTomatoSurvives(true, SoilKind.FLOODED_MUD));
        assertFalse(SoilRules.wildTomatoSurvives(true, null));
        assertFalse(SoilRules.wildTomatoSurvives(false, SoilKind.SANDY));
    }

    @Test
    void shippedTagsMatchTheBuiltInIds() throws IOException {
        assertTag("soils/sandy", SoilKind.SANDY, "minecraft:sand", "minecraft:red_sand");
        assertTag("soils/loam", SoilKind.LOAM, "bed_and_branch:loam");
        assertTag("soils/peat", SoilKind.PEAT, "bed_and_branch:peat");
        assertTag("soils/mud", SoilKind.MUD, "minecraft:mud", "bed_and_branch:mud");
        String flooded = readTag("soils/flooded_mud");
        assertTrue(flooded.contains("\"values\""));
        assertFalse(flooded.contains("bed_and_branch:mud"));
        String bases = readTag("soil_bases");
        assertTrue(bases.contains("#bed_and_branch:soils/sandy"));
        assertTrue(bases.contains("#bed_and_branch:soils/loam"));
        assertTrue(bases.contains("#bed_and_branch:soils/peat"));
        assertTrue(bases.contains("#bed_and_branch:soils/mud"));
        assertTrue(bases.contains("#bed_and_branch:soils/flooded_mud"));
        String supportsCrops = readResource("/data/minecraft/tags/block/supports_crops.json");
        assertTrue(supportsCrops.contains("\"bed_and_branch:farmland\""));
    }

    private static void assertTag(String path, SoilKind kind, String... ids) throws IOException {
        String json = readTag(path);
        for (String id : ids) {
            assertTrue(json.contains("\"" + id + "\""), id);
            assertEquals(Optional.of(kind), SoilRules.kindOf(id));
        }
    }

    private static void assertNamed(String lang, SoilKind kind, String english) {
        assertEquals("block.bed_and_branch.farmland." + kind.serializedName(), kind.farmlandNameKey());
        assertTrue(lang.contains("\"" + kind.farmlandNameKey() + "\": \"" + english + "\""), kind.farmlandNameKey());
    }

    private static String readTag(String path) throws IOException {
        return readResource("/data/bed_and_branch/tags/block/" + path + ".json");
    }

    private static String readResource(String resource) throws IOException {
        try (InputStream in = SoilRulesTest.class.getResourceAsStream(resource)) {
            assertTrue(in != null, resource);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
