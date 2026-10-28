package net.modcrafters.minecraft.bedandbranch.fabric;

import net.modcrafters.minecraft.bedandbranch.Constants;
import net.modcrafters.minecraft.bedandbranch.block.CropStickBlock;
import net.modcrafters.minecraft.bedandbranch.block.SoilFarmlandBlock;
import net.modcrafters.minecraft.bedandbranch.look.PlantLook;
import net.modcrafters.minecraft.bedandbranch.look.PlantLookReader;
import net.modcrafters.minecraft.bedandbranch.look.PlantLookText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin(Constants.MOD_ID)
public final class BedJadePlugin implements IWailaPlugin, IBlockComponentProvider {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "plant");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(this, SoilFarmlandBlock.class);
        registration.registerBlockComponent(this, CropStickBlock.class);
        registration.registerBlockComponent(this, CropBlock.class);
        registration.registerBlockComponent(this, StemBlock.class);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        PlantLook look = PlantLookReader.read(accessor.getLevel(), accessor.getPosition());
        if (look == null) {
            return;
        }
        for (String line : PlantLookText.lines(look)) {
            tooltip.add(Component.literal(line).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public Identifier getUid() {
        return ID;
    }
}
