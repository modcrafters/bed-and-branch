package net.modcrafters.minecraft.bedandbranch.platform;

import net.modcrafters.minecraft.bedandbranch.Constants;
import net.modcrafters.minecraft.bedandbranch.platform.services.PlatformHelper;

import java.util.ServiceLoader;

public final class Services {
    public static final PlatformHelper PLATFORM = load(PlatformHelper.class);

    private Services() {
    }

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz, Services.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Failed to load service for " + clazz.getName()));
    }

    static {
        Constants.LOG.debug("Loaded platform {}", PLATFORM.getClass().getName());
    }
}
