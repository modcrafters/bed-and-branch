package net.modcrafters.minecraft.bedandbranch.block;

import net.modcrafters.minecraft.bedandbranch.item.ModItems;
import net.modcrafters.minecraft.bedandbranch.item.TomatoSeedsItem;
import net.modcrafters.minecraft.bedandbranch.soil.SoilLookup;
import net.modcrafters.minecraft.bedandbranch.soil.SoilRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.List;

public enum StickPlant implements StringRepresentable {
    NONE("none", null, 0),
    TOMATO("tomato", null, 7),
    WHEAT("wheat", Blocks.WHEAT, 7),
    CARROT("carrot", Blocks.CARROTS, 7),
    POTATO("potato", Blocks.POTATOES, 7),
    BEETROOT("beetroot", Blocks.BEETROOTS, 3),
    TORCHFLOWER("torchflower", Blocks.TORCHFLOWER_CROP, 1),
    MELON("melon", Blocks.MELON_STEM, 7),
    PUMPKIN("pumpkin", Blocks.PUMPKIN_STEM, 7);

    public static final EnumProperty<StickPlant> PROPERTY = EnumProperty.create("plant", StickPlant.class);

    private final String serialized;
    private final Block vanilla;
    private final int maxAge;

    StickPlant(String serialized, Block vanilla, int maxAge) {
        this.serialized = serialized;
        this.vanilla = vanilla;
        this.maxAge = maxAge;
    }

    public int maxAge() {
        return maxAge;
    }

    public static StickPlant fromSeed(ItemStack stack) {
        if (stack.getItem() instanceof TomatoSeedsItem) {
            return TOMATO;
        }
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return null;
        }
        Block block = blockItem.getBlock();
        for (StickPlant plant : values()) {
            if (plant.vanilla == block) {
                return plant;
            }
        }
        return null;
    }

    public boolean survives(LevelReader level, BlockPos pos) {
        if (this == NONE) {
            return false;
        }
        if (this == TOMATO) {
            if (level.getRawBrightness(pos, 0) < 8) {
                return false;
            }
            BlockState below = level.getBlockState(pos.below());
            if (!(below.getBlock() instanceof SoilFarmlandBlock)) {
                return false;
            }
            return SoilRules.tomatoSurvives(true, SoilLookup.kindOf(level.getBlockState(pos.below(2))).orElse(null));
        }
        return vanilla.defaultBlockState().canSurvive(level, pos);
    }

    public Item seed() {
        return switch (this) {
            case NONE -> null;
            case TOMATO -> ModItems.TOMATO_SEEDS;
            case WHEAT -> Items.WHEAT_SEEDS;
            case CARROT -> Items.CARROT;
            case POTATO -> Items.POTATO;
            case BEETROOT -> Items.BEETROOT_SEEDS;
            case TORCHFLOWER -> Items.TORCHFLOWER_SEEDS;
            case MELON -> Items.MELON_SEEDS;
            case PUMPKIN -> Items.PUMPKIN_SEEDS;
        };
    }

    public Item fruit() {
        return switch (this) {
            case WHEAT -> Items.WHEAT;
            case CARROT -> Items.CARROT;
            case POTATO -> Items.POTATO;
            case BEETROOT -> Items.BEETROOT;
            case TORCHFLOWER -> Items.TORCHFLOWER;
            case TOMATO -> ModItems.TOMATO;
            default -> null;
        };
    }

    public void growFruit(ServerLevel level, BlockPos pos, RandomSource random) {
        Block fruit = switch (this) {
            case MELON -> Blocks.MELON;
            case PUMPKIN -> Blocks.PUMPKIN;
            default -> null;
        };
        if (fruit == null) {
            return;
        }
        BlockPos fruitPos = pos.relative(Direction.Plane.HORIZONTAL.getRandomDirection(random));
        BlockState ground = level.getBlockState(fruitPos.below());
        if (!level.getBlockState(fruitPos).isAir()) {
            return;
        }
        if (ground.getBlock() instanceof FarmlandBlock || ground.is(BlockTags.DIRT)) {
            level.setBlock(fruitPos, fruit.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    public void addBreakDrops(List<ItemStack> drops, int age) {
        if (this == NONE) {
            return;
        }
        Item fruit = fruit();
        if (age >= maxAge && fruit != null) {
            drops.add(new ItemStack(fruit));
            if (fruit != seed()) {
                drops.add(new ItemStack(seed()));
            }
            return;
        }
        drops.add(new ItemStack(seed()));
    }

    @Override
    public String getSerializedName() {
        return serialized;
    }
}
