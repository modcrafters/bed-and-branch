package net.modcrafters.minecraft.bedandbranch.neoforge.client;

import net.neoforged.bus.api.IEventBus;
//? if <26.1
//import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
//? if <26.1
//import net.minecraft.client.renderer.ItemBlockRenderTypes;
//? if <26.1
//import net.minecraft.client.renderer.RenderType;
//? if <26.1
//import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public final class BedAndBranchNeoForgeClient {
    private BedAndBranchNeoForgeClient() {
    }

    public static void register(IEventBus modBus) {
        // 26.1 takes cutout from the texture. 1.21.1 draws the whole block solid, so the crop's empty pixels turn black.
        //? if <26.1
        //modBus.addListener(BedAndBranchNeoForgeClient::cutoutPlants);
    }

    //? if >=26.1 {
    //?} else {
    /*private static void cutoutPlants(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TOMATO, RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CROP_STICK, RenderType.cutout());
        });
    }
    *///?}
}
