package net.modcrafters.minecraft.bedandbranch.neoforge;

import net.modcrafters.minecraft.bedandbranch.Constants;
import net.modcrafters.minecraft.bedandbranch.neoforge.client.BedAndBranchNeoForgeClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Constants.MOD_ID)
public class BedAndBranchNeoForge {
    public BedAndBranchNeoForge(IEventBus modBus) {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            BedAndBranchNeoForgeClient.register(modBus);
        }
        Constants.LOG.info("{} loaded", Constants.MOD_NAME);
    }
}
