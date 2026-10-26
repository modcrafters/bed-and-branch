package net.modcrafters.minecraft.bedandbranch.soil;

import net.modcrafters.minecraft.bedandbranch.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    public static final TagKey<Block> SOIL_BASES = block("soil_bases");
    public static final TagKey<Block> SOILS_SANDY = block("soils/sandy");
    public static final TagKey<Block> SOILS_LOAM = block("soils/loam");
    public static final TagKey<Block> SOILS_PEAT = block("soils/peat");
    public static final TagKey<Block> SOILS_FLOODED_MUD = block("soils/flooded_mud");

    private ModTags() {
    }

    private static TagKey<Block> block(String path) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Constants.MOD_ID, path));
    }
}
