package net.modcrafters.minecraft.bedandbranch.fabric;

import net.modcrafters.minecraft.bedandbranch.Constants;
import net.fabricmc.api.ModInitializer;

public class BedAndBranchFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Constants.LOG.info("{} loaded", Constants.MOD_NAME);
    }
}
