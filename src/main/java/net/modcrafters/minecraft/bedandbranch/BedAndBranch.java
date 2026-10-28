package net.modcrafters.minecraft.bedandbranch;

import net.modcrafters.minecraft.bedandbranch.config.BedAndBranchConfig;
import net.modcrafters.minecraft.bedandbranch.platform.Services;

public final class BedAndBranch {
    private BedAndBranch() {
    }

    public static void init() {
        BedAndBranchConfig.load(Services.PLATFORM.configDirectory().resolve("bed_and_branch.json"));
        Constants.LOG.info("{} common init", Constants.MOD_NAME);
    }
}
