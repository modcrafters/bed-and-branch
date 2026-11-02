package net.modcrafters.minecraft.bedandbranch.worldgen;

import net.modcrafters.minecraft.bedandbranch.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
//? if >=26.3
import com.mojang.serialization.MapCodec;

import java.util.function.BiConsumer;

public final class ModFarms {
    public static final TagKey<Biome> BIOMES = TagKey.create(Registries.BIOME, id("has_abandoned_rice_farm"));

    private ModFarms() { }

    public static Identifier id(String name) { return Identifier.fromNamespaceAndPath(Constants.MOD_ID, name); }
    public static ResourceKey<PlacedFeature> placed(FarmLayout layout) {
        return ResourceKey.create(Registries.PLACED_FEATURE, id(layout.id()));
    }

    //? if >=26.3 {
    public static void register(BiConsumer<Identifier, MapCodec<? extends Feature>> registry) {
        for (FarmLayout layout : FarmLayout.values()) registry.accept(id(layout.id()), new AbandonedFarmFeature(layout).codec());
    }
    //?} else {
    /*public static void register(BiConsumer<Identifier, Feature<?>> registry) {
        for (FarmLayout layout : FarmLayout.values()) registry.accept(id(layout.id()), new AbandonedFarmFeature(layout));
    }
    *///?}
}
