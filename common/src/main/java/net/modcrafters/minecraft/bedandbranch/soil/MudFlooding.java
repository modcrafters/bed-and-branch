package net.modcrafters.minecraft.bedandbranch.soil;

import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Optional;

public final class MudFlooding {
    private MudFlooding() {
    }

    public static Optional<InteractionResult> flood(Player player, InteractionHand hand, BlockHitResult hit) {
        if (player == null || !player.getItemInHand(hand).is(Items.WATER_BUCKET)) {
            return Optional.empty();
        }
        if (hit.getType() != HitResult.Type.BLOCK) {
            return Optional.empty();
        }
        Level level = player.level();
        BlockPos pos = hit.getBlockPos();
        if (!level.getBlockState(pos).is(Blocks.MUD) || !level.mayInteract(player, pos)) {
            return Optional.empty();
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, ModBlocks.FLOODED_MUD.defaultBlockState(), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        if (player.hasInfiniteMaterials()) {
            return Optional.of(InteractionResult.SUCCESS);
        }
        return Optional.of(InteractionResult.SUCCESS.heldItemTransformedTo(new ItemStack(Items.BUCKET)));
    }
}
