package net.modcrafters.minecraft.bedandbranch.command;

import net.modcrafters.minecraft.bedandbranch.soil.SoilKind;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BedInfoTextTest {
    @Test
    void reportsBaseFarmlandAndStick() {
        assertEquals(
                "Base: loam. Farmland: yes. Stick: yes.",
                BedInfoText.format(Optional.of(SoilKind.LOAM), true, true)
        );
        assertEquals(
                "Base: flooded mud. Farmland: no. Stick: no.",
                BedInfoText.format(Optional.of(SoilKind.FLOODED_MUD), false, false)
        );
        assertEquals(
                "Base: none. Farmland: no. Stick: no.",
                BedInfoText.format(Optional.empty(), false, false)
        );
    }
}
