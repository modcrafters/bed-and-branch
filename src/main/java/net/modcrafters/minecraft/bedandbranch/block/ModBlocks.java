package net.modcrafters.minecraft.bedandbranch.block;

import net.modcrafters.minecraft.bedandbranch.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.BiConsumer;

public final class ModBlocks {
    public static final ResourceKey<Block> LOAM_ID = blockId("loam");
    public static final ResourceKey<Block> PEAT_ID = blockId("peat");
    public static final ResourceKey<Block> MUD_ID = blockId("mud");
    public static final ResourceKey<Block> FARMLAND_ID = blockId("farmland");
    public static final ResourceKey<Block> TOMATO_ID = blockId("tomato");
    public static final ResourceKey<Block> CROP_STICK_ID = blockId("crop_stick");

    public static final Block LOAM = new Block(earth(LOAM_ID, SoundType.GRAVEL));
    public static final Block PEAT = new Block(earth(PEAT_ID, SoundType.ROOTED_DIRT));
    public static final MudBlock MUD = new MudBlock(earth(MUD_ID, SoundType.MUD));
    public static final SoilFarmlandBlock FARMLAND = new SoilFarmlandBlock(farmland());
    public static final TomatoCropBlock TOMATO = new TomatoCropBlock(crop(TOMATO_ID).randomTicks());
    public static final CropStickBlock CROP_STICK = new CropStickBlock(crop(CROP_STICK_ID).randomTicks().lightLevel(CropStickBlock::lightEmission));

    private ModBlocks() {
    }

    public static void register(BiConsumer<Identifier, Block> out) {
        out.accept(LOAM_ID.identifier(), LOAM);
        out.accept(PEAT_ID.identifier(), PEAT);
        out.accept(MUD_ID.identifier(), MUD);
        out.accept(FARMLAND_ID.identifier(), FARMLAND);
        out.accept(TOMATO_ID.identifier(), TOMATO);
        out.accept(CROP_STICK_ID.identifier(), CROP_STICK);
    }

    private static ResourceKey<Block> blockId(String path) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Constants.MOD_ID, path));
    }

    private static BlockBehaviour.Properties earth(ResourceKey<Block> id, SoundType sound) {
        return BlockBehaviour.Properties.of()
                //? if >=1.21.2
                .setId(id)
                .strength(0.5F)
                .sound(sound);
    }

    private static BlockBehaviour.Properties farmland() {
        var properties = BlockBehaviour.Properties.of()
                //? if >=1.21.2
                .setId(FARMLAND_ID)
                .strength(0.6F)
                .sound(SoundType.GRAVEL)
                .randomTicks();
        // Looking at the block uses the picked item name, which includes the soil.
        // A translation for this id would hide that name. 1.21.1 has no overrideDescription.
        //? if >=1.21.2
        properties = properties.overrideDescription("block.bed_and_branch.farmland.by_soil");
        return properties;
    }

    private static BlockBehaviour.Properties crop(ResourceKey<Block> id) {
        return BlockBehaviour.Properties.of()
                //? if >=1.21.2
                .setId(id)
                .instabreak()
                .noCollision()
                .noOcclusion()
                .sound(SoundType.CROP)
                // Pistons pop the stick. 1.21.1 has no POPPED, so the stick breaks instead.
                //? if >=1.21.2
                .pushReaction(PushReaction.POPPED);
                //? if <1.21.2
                //.pushReaction(PushReaction.DESTROY);
    }
}
