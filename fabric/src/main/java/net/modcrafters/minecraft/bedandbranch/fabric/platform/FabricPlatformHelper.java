package net.modcrafters.minecraft.bedandbranch.fabric.platform;

import net.modcrafters.minecraft.bedandbranch.platform.services.PlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class FabricPlatformHelper implements PlatformHelper {
    @Override
    public Path configDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
