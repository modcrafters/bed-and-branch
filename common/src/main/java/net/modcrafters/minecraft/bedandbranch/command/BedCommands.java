package net.modcrafters.minecraft.bedandbranch.command;

import net.modcrafters.minecraft.bedandbranch.block.ModBlocks;
import net.modcrafters.minecraft.bedandbranch.block.TomatoCropBlock;
import net.modcrafters.minecraft.bedandbranch.soil.SoilKind;
import net.modcrafters.minecraft.bedandbranch.soil.SoilLookup;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public final class BedCommands {
    private BedCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bedbranch")
                .requires(source -> true)
                .then(Commands.literal("info").executes(context -> info(context.getSource()))));
    }

    private static int info(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("A player is needed."));
            return 0;
        }
        Scan scan = scan(player);
        source.sendSuccess(() -> Component.literal(BedInfoText.format(scan.base, scan.farmland, scan.stick)), false);
        return 1;
    }

    private static Scan scan(ServerPlayer player) {
        BlockPos origin = player.blockPosition();
        boolean farmland = false;
        boolean stick = false;
        Optional<SoilKind> base = Optional.empty();
        for (int i = 0; i <= 2; i++) {
            BlockPos pos = origin.below(i);
            BlockState state = player.level().getBlockState(pos);
            if (!farmland && state.is(ModBlocks.FARMLAND)) {
                farmland = true;
                base = SoilLookup.kindOf(player.level().getBlockState(pos.below()));
            }
            if (isStick(state)) {
                stick = true;
            }
        }
        if (!farmland) {
            base = SoilLookup.kindOf(player.level().getBlockState(origin.below()));
        }
        return new Scan(base, farmland, stick);
    }

    private static boolean isStick(BlockState state) {
        if (state.is(ModBlocks.CROP_STICK)) {
            return true;
        }
        return state.is(ModBlocks.TOMATO) && state.getValue(TomatoCropBlock.STAKED);
    }

    private record Scan(Optional<SoilKind> base, boolean farmland, boolean stick) {
    }
}
