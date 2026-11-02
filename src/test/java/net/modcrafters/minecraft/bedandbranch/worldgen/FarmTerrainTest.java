package net.modcrafters.minecraft.bedandbranch.worldgen;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class FarmTerrainTest {
    private int[][] terrain(FarmLayout layout, boolean followTerrace) {
        int[][] heights = new int[layout.depth()][layout.width()];
        for (int z = 0; z < layout.depth(); z++) for (int x = 0; x < layout.width(); x++)
            heights[z][x] = 64 + (followTerrace ? layout.elevation(x, z) : 0) - (layout.creek(x, z) ? 1 : 0);
        return heights;
    }

    @Test void eachLayoutFitsItsIntendedTerrain() {
        for (FarmLayout layout : FarmLayout.values())
            assertEquals(64, FarmTerrain.fit(layout, terrain(layout, true)).orElseThrow(), layout.id());
    }

    @Test void terracesRequireANaturalRiseRatherThanAnArtificialPlatform() {
        assertTrue(FarmTerrain.fit(FarmLayout.TERRACED_PATCH, terrain(FarmLayout.TERRACED_PATCH, false)).isEmpty());
        int[][] reversed = terrain(FarmLayout.TERRACED_PATCH, true);
        for (int z = 0; z < reversed.length; z++) for (int x = 0; x < reversed[z].length; x++)
            reversed[z][x] = z < 4 ? 64 : 65;
        assertTrue(FarmTerrain.fit(FarmLayout.TERRACED_PATCH, reversed).isEmpty());
    }

    @Test void smallGroundIrregularitiesAreAllowedButCliffsAndHolesAreNot() {
        int[][] heights = terrain(FarmLayout.POCKET_PADDY, true);
        heights[0][0]++;
        heights[3][2]--;
        assertTrue(FarmTerrain.fit(FarmLayout.POCKET_PADDY, heights).isPresent());
        heights[3][2]--;
        assertTrue(FarmTerrain.fit(FarmLayout.POCKET_PADDY, heights).isEmpty());
        heights[3][2] = 64;
        heights[0][0] = 67;
        assertTrue(FarmTerrain.fit(FarmLayout.POCKET_PADDY, heights).isEmpty());
    }

    @Test void widespreadEarthworksAreRejectedEvenWhenEveryColumnIsClose() {
        int[][] heights = terrain(FarmLayout.SPLIT_BEDS, true);
        for (int z = 0; z < heights.length; z++) for (int x = 0; x < heights[z].length; x++)
            heights[z][x] += (x + z) % 2;
        assertTrue(FarmTerrain.fit(FarmLayout.SPLIT_BEDS, heights).isEmpty());
    }

    @Test void creekMustSitImmediatelyBelowTheBank() {
        int[][] heights = terrain(FarmLayout.CREEKSIDE_PLOT, true);
        heights[1][0] = 61;
        assertTrue(FarmTerrain.fit(FarmLayout.CREEKSIDE_PLOT, heights).isEmpty());
        heights[1][0] = 64;
        assertTrue(FarmTerrain.fit(FarmLayout.CREEKSIDE_PLOT, heights).isEmpty());
    }

    @Test void rotationsPreserveEveryCellAndEachFarmHasRiceAndRecoverableSoil() {
        for (FarmLayout layout : FarmLayout.values()) {
            boolean rice = false, loam = false;
            for (int rotation = 0; rotation < 4; rotation++) {
                var coordinates = new HashSet<String>();
                for (int z = 0; z < layout.depth(); z++) for (int x = 0; x < layout.width(); x++) {
                    assertTrue(coordinates.add(layout.worldX(x,z,rotation) + "," + layout.worldZ(x,z,rotation)));
                    assertTrue(".REMILPFCOWB".indexOf(layout.cell(x,z)) >= 0);
                    rice |= layout.cell(x,z) == 'R';
                    loam |= layout.cell(x,z) == 'L';
                }
            }
            assertTrue(rice && loam, layout.id());
        }
    }

    @Test void allBedsStayHydratedAndIrrigationDoesNotSpillOverATerraceEdge() {
        for (FarmLayout layout : FarmLayout.values()) {
            for (int z = 0; z < layout.depth(); z++) for (int x = 0; x < layout.width(); x++) {
                char cell = layout.cell(x,z);
                if (cell == 'I') {
                    assertTrue(x > 0 && x < layout.width()-1 && z > 0 && z < layout.depth()-1);
                    for (int[] step : new int[][]{{-1,0},{1,0},{0,-1},{0,1}})
                        assertEquals(layout.elevation(x,z), layout.elevation(x+step[0],z+step[1]), layout.id());
                }
                if ("REM".indexOf(cell) < 0) continue;
                boolean watered = false;
                for (int wz = 0; wz < layout.depth(); wz++) for (int wx = 0; wx < layout.width(); wx++) {
                    watered |= layout.cell(wx,wz) == 'I' && Math.abs(wx-x) <= 4 && Math.abs(wz-z) <= 4
                            && layout.elevation(x,z) == layout.elevation(wx,wz);
                }
                assertTrue(watered, layout.id() + " " + x + "," + z);
            }
        }
    }
}
