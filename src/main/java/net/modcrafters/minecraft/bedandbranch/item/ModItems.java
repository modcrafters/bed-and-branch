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
    public static final ResourceKey<Item> FARMLAND_ID = itemId("farmland");
    public static final ResourceKey<Item> NETHER_FARMLAND_ID = itemId("nether_farmland");
    public static final ResourceKey<Item> CROP_STICK_ID = itemId("crop_stick");
    public static final ResourceKey<Item> BERRY_OBELISK_ID = itemId("berry_obelisk");
    public static final ResourceKey<Item> VINE_TRELLIS_ID = itemId("vine_trellis");
    public static final ResourceKey<Item> TOMATO_SEEDS_ID = itemId("tomato_seeds");
    public static final ResourceKey<Item> TOMATO_ID = itemId("tomato");
    public static final ResourceKey<Item> RICE_SEEDS_ID = itemId("rice_seeds");
    public static final ResourceKey<Item> RICE_ID = itemId("rice");
    public static final ResourceKey<Item> CORN_SEEDS_ID = itemId("corn_seeds");
    public static final ResourceKey<Item> CORN_ID = itemId("corn");
    public static final ResourceKey<Item> DEBUG_STICK_NEXT_ID = itemId("debug_stick_next");
    public static final ResourceKey<Item> DEBUG_STICK_PREVIOUS_ID = itemId("debug_stick_previous");
    public static final ResourceKey<Item> DEBUG_STICK_SPREAD_ID = itemId("debug_stick_spread");

    public static final Item LOAM = blockItem(ModBlocks.LOAM, LOAM_ID);
    public static final Item PEAT = blockItem(ModBlocks.PEAT, PEAT_ID);
    public static final Item FARMLAND = blockItem(ModBlocks.FARMLAND, FARMLAND_ID);
    public static final Item NETHER_FARMLAND = blockItem(ModBlocks.NETHER_FARMLAND, NETHER_FARMLAND_ID);
    public static final Item CROP_STICK = new CropStickItem(ModBlocks.CROP_STICK, properties(CROP_STICK_ID));
    public static final Item BERRY_OBELISK = new GardenSupportItem(ModBlocks.BERRY_OBELISK, properties(BERRY_OBELISK_ID));
    public static final Item VINE_TRELLIS = new GardenSupportItem(ModBlocks.VINE_TRELLIS, properties(VINE_TRELLIS_ID));
    public static final Item TOMATO_SEEDS = new TomatoSeedsItem(properties(TOMATO_SEEDS_ID));
    public static final Item TOMATO = new Item(properties(TOMATO_ID));
    public static final Item RICE_SEEDS = new RiceSeedsItem(properties(RICE_SEEDS_ID));
    public static final Item RICE = new Item(properties(RICE_ID));
    public static final Item CORN_SEEDS = new CornSeedsItem(properties(CORN_SEEDS_ID));
    public static final Item CORN = new Item(properties(CORN_ID));
    public static final Item DEBUG_STICK_NEXT = new StageStickItem(1, properties(DEBUG_STICK_NEXT_ID));
    public static final Item DEBUG_STICK_PREVIOUS = new StageStickItem(-1, properties(DEBUG_STICK_PREVIOUS_ID));
    public static final Item DEBUG_STICK_SPREAD = new SpreadStickItem(properties(DEBUG_STICK_SPREAD_ID));

    private ModItems() {
    }

    public static void register(BiConsumer<Identifier, Item> out) {
        out.accept(LOAM_ID.identifier(), LOAM);
        out.accept(PEAT_ID.identifier(), PEAT);
        out.accept(FARMLAND_ID.identifier(), FARMLAND);
        out.accept(NETHER_FARMLAND_ID.identifier(), NETHER_FARMLAND);
        out.accept(CROP_STICK_ID.identifier(), CROP_STICK);
        out.accept(BERRY_OBELISK_ID.identifier(), BERRY_OBELISK);
        out.accept(VINE_TRELLIS_ID.identifier(), VINE_TRELLIS);
        out.accept(TOMATO_SEEDS_ID.identifier(), TOMATO_SEEDS);
        out.accept(TOMATO_ID.identifier(), TOMATO);
        out.accept(RICE_SEEDS_ID.identifier(), RICE_SEEDS);
        out.accept(RICE_ID.identifier(), RICE);
        out.accept(CORN_SEEDS_ID.identifier(), CORN_SEEDS);
        out.accept(CORN_ID.identifier(), CORN);
        out.accept(DEBUG_STICK_NEXT_ID.identifier(), DEBUG_STICK_NEXT);
        out.accept(DEBUG_STICK_PREVIOUS_ID.identifier(), DEBUG_STICK_PREVIOUS);
        out.accept(DEBUG_STICK_SPREAD_ID.identifier(), DEBUG_STICK_SPREAD);
    }

    public static List<ItemStack> tabContents() {
        return List.of(
                new ItemStack(LOAM),
                new ItemStack(PEAT),
                new ItemStack(FARMLAND),
                new ItemStack(NETHER_FARMLAND),
                new ItemStack(CROP_STICK),
                new ItemStack(BERRY_OBELISK),
                new ItemStack(VINE_TRELLIS),
                new ItemStack(TOMATO_SEEDS),
                new ItemStack(TOMATO),
                new ItemStack(RICE_SEEDS),
                new ItemStack(RICE),
                new ItemStack(CORN_SEEDS),
                new ItemStack(CORN),
                new ItemStack(DEBUG_STICK_NEXT),
                new ItemStack(DEBUG_STICK_PREVIOUS),
                new ItemStack(DEBUG_STICK_SPREAD)
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
