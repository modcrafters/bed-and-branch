package net.modcrafters.minecraft.bedandbranch.fabric.client;

import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSources;

import java.util.List;

public class BedAndBranchFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockColorRegistry.register(List.of(BlockTintSources.water()), ModBlocks.FARMLAND);
        BlockColorRegistry.register(List.of(BlockTintSources.foliage()), ModBlocks.VINE_TRELLIS);
    }
}
