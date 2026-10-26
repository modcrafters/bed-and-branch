package net.modcrafters.minecraft.bedandbranch.neoforge.platform;

import net.modcrafters.minecraft.bedandbranch.platform.services.PlatformHelper;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class NeoForgePlatformHelper implements PlatformHelper {
    @Override
    public Path configDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
