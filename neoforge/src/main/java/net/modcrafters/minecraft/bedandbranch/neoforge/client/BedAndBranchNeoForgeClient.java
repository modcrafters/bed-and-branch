package net.modcrafters.minecraft.bedandbranch.neoforge.client;

import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
//? if >=26.1 {
import net.minecraft.client.color.block.BlockTintSources;

import java.util.List;
//?} else {
/*import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
*///?}

public final class BedAndBranchNeoForgeClient {
    private BedAndBranchNeoForgeClient() {
    }

    public static void register(IEventBus modBus) {
        // 26.1 takes cutout from the texture. 1.21.1 draws the whole block solid, so the crop's empty pixels turn black.
        //? if <26.1
        //modBus.addListener(BedAndBranchNeoForgeClient::cutoutPlants);
        modBus.addListener(BedAndBranchNeoForgeClient::trenchWaterColor);
    }

    // Trench water uses the water texture with tint index 0. Other farmland faces leave the tint unset.
    //? if >=26.1 {
    private static void trenchWaterColor(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(BlockTintSources.water()), ModBlocks.FARMLAND);
    }
    //?} else {
    /*private static void trenchWaterColor(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
            if (level == null || pos == null) {
                return 0x3F76E4;
            }
            return BiomeColors.getAverageWaterColor(level, pos);
        }, ModBlocks.FARMLAND);
    }
    *///?}

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
