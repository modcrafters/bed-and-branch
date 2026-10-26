package net.modcrafters.minecraft.bedandbranch.soil;

import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public final class SoilTilling {
    private SoilTilling() {
    }

    public static Optional<InteractionResult> till(UseOnContext context) {
        if (!context.getItemInHand().is(ItemTags.HOES) || context.getClickedFace() == Direction.DOWN) {
            return Optional.empty();
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState target = level.getBlockState(pos);
        BlockState base = level.getBlockState(pos.below());
        if (!target.is(Blocks.DIRT)) {
            return Optional.empty();
        }
        SoilKind kind = SoilLookup.kindOf(base).orElse(null);
        if (kind == null || !level.getBlockState(pos.above()).isAir()) {
            return Optional.empty();
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, ModBlocks.FARMLAND.defaultBlockState().setValue(SoilState.PROPERTY, SoilState.of(kind)), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.HOE_TILL.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
            Player player = context.getPlayer();
            if (player != null && !player.hasInfiniteMaterials()) {
                context.getItemInHand().hurtAndBreak(1, player, context.getHand());
            }
        }
        return Optional.of(InteractionResult.SUCCESS);
    }
}
