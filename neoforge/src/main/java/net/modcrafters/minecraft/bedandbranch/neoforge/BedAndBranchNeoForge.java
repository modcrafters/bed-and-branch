package net.modcrafters.minecraft.bedandbranch.neoforge;

import net.modcrafters.minecraft.bedandbranch.BedAndBranch;
import net.modcrafters.minecraft.bedandbranch.Constants;
import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.modcrafters.minecraft.bedandbranch.command.BedCommands;
import net.modcrafters.minecraft.bedandbranch.item.ModItems;
import net.modcrafters.minecraft.bedandbranch.item.ModTab;
import net.modcrafters.minecraft.bedandbranch.neoforge.client.BedAndBranchNeoForgeClient;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
public class BedAndBranchNeoForge {
    public BedAndBranchNeoForge(IEventBus modBus) {
        modBus.addListener(BedAndBranchNeoForge::registerContent);
        //? if >=1.21.9 {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
        //?} else
        //if (FMLEnvironment.dist == Dist.CLIENT) {
            BedAndBranchNeoForgeClient.register(modBus);
        }
        BedAndBranch.init();
        NeoForge.EVENT_BUS.addListener(BedAndBranchNeoForge::onRegisterCommands);
    }

    private static void registerContent(RegisterEvent event) {
        event.register(Registries.BLOCK, helper -> ModBlocks.register(helper::register));
        event.register(Registries.ITEM, helper -> ModItems.register(helper::register));
        event.register(Registries.CREATIVE_MODE_TAB, ModTab.ID.identifier(), () -> ModTab.titled(CreativeModeTab.builder())
                .displayItems((parameters, output) -> ModItems.tabContents().forEach(output::accept))
                .build());
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        BedCommands.register(event.getDispatcher());
    }
}
