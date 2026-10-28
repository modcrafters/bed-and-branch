package net.modcrafters.minecraft.bedandbranch.item;

import net.modcrafters.minecraft.bedandbranch.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public final class ModTab {
    public static final ResourceKey<CreativeModeTab> ID = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, Constants.MOD_ID)
    );

    private ModTab() {
    }

    public static CreativeModeTab.Builder titled(CreativeModeTab.Builder builder) {
        return builder
                .title(Component.translatable("itemGroup." + Constants.MOD_ID))
                .icon(ModItems::tabIcon);
    }
}
