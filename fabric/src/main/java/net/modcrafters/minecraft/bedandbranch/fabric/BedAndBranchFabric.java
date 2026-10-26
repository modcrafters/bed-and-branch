package net.modcrafters.minecraft.bedandbranch.fabric;

import net.modcrafters.minecraft.bedandbranch.BedAndBranch;
import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.modcrafters.minecraft.bedandbranch.command.BedCommands;
import net.modcrafters.minecraft.bedandbranch.item.ModItems;
import net.modcrafters.minecraft.bedandbranch.item.ModTab;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class BedAndBranchFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModBlocks.register((id, block) -> Registry.register(BuiltInRegistries.BLOCK, id, block));
        ModItems.register((id, item) -> Registry.register(BuiltInRegistries.ITEM, id, item));
        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                ModTab.ID.identifier(),
                ModTab.titled(FabricCreativeModeTab.builder())
                        .displayItems((parameters, output) -> ModItems.tabContents().forEach(output::accept))
                        .build()
        );
        BedAndBranch.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> BedCommands.register(dispatcher));
    }
}
