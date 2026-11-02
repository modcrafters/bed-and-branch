package net.modcrafters.minecraft.bedandbranch.worldgen;

import java.util.Arrays;
import java.util.OptionalInt;

/** Pure terrain fitting rules; sampling and all block writes live in the feature. */
public final class FarmTerrain {
    private FarmTerrain() { }

    public static OptionalInt fit(FarmLayout layout, int[][] heights) {
        int[] bases = new int[layout.width() * layout.depth()];
        int count = 0;
        double upper = 0, lower = 0;
        int upperCount = 0, lowerCount = 0;
        for (int z = 0; z < layout.depth(); z++) {
            for (int x = 0; x < layout.width(); x++) {
                if (layout.creek(x, z)) continue;
                int offset = layout.elevation(x, z);
                bases[count++] = heights[z][x] - offset;
                if (offset == 1) { upper += heights[z][x]; upperCount++; }
                else { lower += heights[z][x]; lowerCount++; }
            }
        }
        Arrays.sort(bases, 0, count);
        int base = bases[count / 2];
        int earthwork = 0;
        for (int z = 0; z < layout.depth(); z++) {
            for (int x = 0; x < layout.width(); x++) {
                int target = base + layout.elevation(x, z);
                if (layout.creek(x, z)) {
                    // The existing creek surface must be just below the bank/bridge.
                    if (heights[z][x] != base - 1) return OptionalInt.empty();
                    continue;
                }
                int difference = Math.abs(heights[z][x] - target);
                if (difference > 1) return OptionalInt.empty();
                earthwork += difference;
            }
        }
        // At least three quarters of the footprint must already be at its planned height.
        if (earthwork > count / 4) return OptionalInt.empty();
        if (layout == FarmLayout.TERRACED_PATCH) {
            double rise = upper / upperCount - lower / lowerCount;
            if (rise < .75 || rise > 1.25) return OptionalInt.empty();
        }
        return OptionalInt.of(base);
    }
}
