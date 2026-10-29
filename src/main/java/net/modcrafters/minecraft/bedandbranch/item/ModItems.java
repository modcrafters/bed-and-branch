package net.modcrafters.minecraft.bedandbranch.item;

import net.modcrafters.minecraft.bedandbranch.Constants;
import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.BiConsumer;

public final class ModItems {
    public static final ResourceKey<Item> LOAM_ID = itemId("loam");
    public static final ResourceKey<Item> PEAT_ID = itemId("peat");
    public static final ResourceKey<Item> MUD_ID = itemId("mud");
    public static final ResourceKey<Item> FARMLAND_ID = itemId("farmland");
    public static final ResourceKey<Item> CROP_STICK_ID = itemId("crop_stick");
    public static final ResourceKey<Item> TOMATO_SEEDS_ID = itemId("tomato_seeds");
    public static final ResourceKey<Item> TOMATO_ID = itemId("tomato");
    public static final ResourceKey<Item> DEBUG_STICK_NEXT_ID = itemId("debug_stick_next");
    public static final ResourceKey<Item> DEBUG_STICK_PREVIOUS_ID = itemId("debug_stick_previous");

    public static final Item LOAM = blockItem(ModBlocks.LOAM, LOAM_ID);
    public static final Item PEAT = blockItem(ModBlocks.PEAT, PEAT_ID);
    public static final Item MUD = blockItem(ModBlocks.MUD, MUD_ID);
    public static final Item FARMLAND = blockItem(ModBlocks.FARMLAND, FARMLAND_ID);
    public static final Item CROP_STICK = new CropStickItem(ModBlocks.CROP_STICK, properties(CROP_STICK_ID));
    public static final Item TOMATO_SEEDS = new TomatoSeedsItem(properties(TOMATO_SEEDS_ID));
    public static final Item TOMATO = new Item(properties(TOMATO_ID));
    public static final Item DEBUG_STICK_NEXT = new StageStickItem(1, properties(DEBUG_STICK_NEXT_ID));
    public static final Item DEBUG_STICK_PREVIOUS = new StageStickItem(-1, properties(DEBUG_STICK_PREVIOUS_ID));

    private ModItems() {
    }

    public static void register(BiConsumer<Identifier, Item> out) {
        out.accept(LOAM_ID.identifier(), LOAM);
        out.accept(PEAT_ID.identifier(), PEAT);
        out.accept(MUD_ID.identifier(), MUD);
        out.accept(FARMLAND_ID.identifier(), FARMLAND);
        out.accept(CROP_STICK_ID.identifier(), CROP_STICK);
        out.accept(TOMATO_SEEDS_ID.identifier(), TOMATO_SEEDS);
        out.accept(TOMATO_ID.identifier(), TOMATO);
        out.accept(DEBUG_STICK_NEXT_ID.identifier(), DEBUG_STICK_NEXT);
        out.accept(DEBUG_STICK_PREVIOUS_ID.identifier(), DEBUG_STICK_PREVIOUS);
    }

    public static List<ItemStack> tabContents() {
        return List.of(
                new ItemStack(LOAM),
                new ItemStack(PEAT),
                new ItemStack(MUD),
                new ItemStack(FARMLAND),
                new ItemStack(CROP_STICK),
                new ItemStack(TOMATO_SEEDS),
                new ItemStack(TOMATO),
                new ItemStack(DEBUG_STICK_NEXT),
                new ItemStack(DEBUG_STICK_PREVIOUS)
        );
    }

    public static ItemStack tabIcon() {
        return new ItemStack(TOMATO);
    }

    // Items know their id from their properties since 1.21.2; before that the registry names them.
    private static Item.Properties properties(ResourceKey<Item> id) {
        //? if >=1.21.2 {
        return new Item.Properties().setId(id);
        //?} else
        //return new Item.Properties();
    }

    private static Item blockItem(Block block, ResourceKey<Item> id) {
        return new BlockItem(block, properties(id));
    }

    private static ResourceKey<Item> itemId(String path) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Constants.MOD_ID, path));
    }
}
