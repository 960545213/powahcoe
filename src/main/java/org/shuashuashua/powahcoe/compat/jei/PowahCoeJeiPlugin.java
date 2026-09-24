package org.shuashuashua.powahcoe.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.world.level.ItemLike;
import org.shuashuashua.powahcoe.Powahcoe;
import owmii.powah.compat.jei.energizing.EnergizingCategory;

@mezz.jei.api.JeiPlugin
public class PowahCoeJeiPlugin implements IModPlugin {

    @Override
    public net.minecraft.resources.ResourceLocation getPluginUid() {
        return new net.minecraft.resources.ResourceLocation(Powahcoe.MODID, "jei_plugin");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        var type = EnergizingCategory.TYPE;
        reg.addRecipeCatalyst(Powahcoe.CABLE_ORB_STARTER_ITEM.get(), type);
        reg.addRecipeCatalyst(Powahcoe.CABLE_ORB_BASIC_ITEM.get(), type);
        reg.addRecipeCatalyst(Powahcoe.CABLE_ORB_HARDENED_ITEM.get(), type);
        reg.addRecipeCatalyst(Powahcoe.CABLE_ORB_BLAZING_ITEM.get(), type);
        reg.addRecipeCatalyst(Powahcoe.CABLE_ORB_NIOTIC_ITEM.get(), type);
        reg.addRecipeCatalyst(Powahcoe.CABLE_ORB_SPIRITED_ITEM.get(), type);
        reg.addRecipeCatalyst(Powahcoe.CABLE_ORB_NITRO_ITEM.get(), type);
        reg.addRecipeCatalyst(Powahcoe.CABLE_ORB_STARTER_X5_ITEM.get(), type);
        reg.addRecipeCatalyst(Powahcoe.CABLE_ORB_BASIC_X5_ITEM.get(), type);
        reg.addRecipeCatalyst(Powahcoe.CABLE_ORB_HARDENED_X5_ITEM.get(), type);
        reg.addRecipeCatalyst(Powahcoe.CABLE_ORB_BLAZING_X5_ITEM.get(), type);
        reg.addRecipeCatalyst(Powahcoe.CABLE_ORB_NIOTIC_X5_ITEM.get(), type);
        reg.addRecipeCatalyst(Powahcoe.CABLE_ORB_SPIRITED_X5_ITEM.get(), type);
        reg.addRecipeCatalyst(Powahcoe.CABLE_ORB_NITRO_X5_ITEM.get(), type);
    }
}
