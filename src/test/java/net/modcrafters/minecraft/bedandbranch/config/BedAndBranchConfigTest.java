package net.modcrafters.minecraft.bedandbranch.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BedAndBranchConfigTest {
    @Test
    void missingFileIsCreatedWithTheOverrideOn() throws IOException {
        Path file = Files.createTempDirectory("bed-and-branch").resolve("bed_and_branch.json");

        BedAndBranchConfig.load(file);

        assertTrue(Files.exists(file));
        assertTrue(BedAndBranchConfig.parse(Files.readString(file)).vanillaCropOverride());
        assertTrue(BedAndBranchConfig.current().vanillaCropOverride());
    }

    @Test
    void brokenFileKeepsTheDefaultAndLeavesTheFileAlone() throws IOException {
        Path file = Files.createTempDirectory("bed-and-branch").resolve("bed_and_branch.json");
        Files.writeString(file, "{");

        BedAndBranchConfig.load(file);

        assertEquals("{", Files.readString(file));
        assertTrue(BedAndBranchConfig.current().vanillaCropOverride());
    }

    @Test
    void storedFalseStaysFalse() throws IOException {
        Path file = Files.createTempDirectory("bed-and-branch").resolve("bed_and_branch.json");
        Files.writeString(file, "{\"vanillaCropOverride\":false}");

        BedAndBranchConfig.load(file);

        assertEquals(false, BedAndBranchConfig.current().vanillaCropOverride());
    }
}
