package org.PowahCOE.coe.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.resources.ResourceLocation;
import org.PowahCOE.coe.PowahCOE;
import owmii.powah.compat.jei.JeiEnergizingCategory;

@JeiPlugin
public class PowahCoeJeiPlugin implements IModPlugin {
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        var type = JeiEnergizingCategory.TYPE.get();
        registration.addRecipeCatalyst(PowahCOE.CABLE_ORB_STARTER_ITEM.get(), type);
        registration.addRecipeCatalyst(PowahCOE.CABLE_ORB_BASIC_ITEM.get(), type);
        registration.addRecipeCatalyst(PowahCOE.CABLE_ORB_HARDENED_ITEM.get(), type);
        registration.addRecipeCatalyst(PowahCOE.CABLE_ORB_BLAZING_ITEM.get(), type);
        registration.addRecipeCatalyst(PowahCOE.CABLE_ORB_NIOTIC_ITEM.get(), type);
        registration.addRecipeCatalyst(PowahCOE.CABLE_ORB_SPIRITED_ITEM.get(), type);
        registration.addRecipeCatalyst(PowahCOE.CABLE_ORB_NITRO_ITEM.get(), type);
        registration.addRecipeCatalyst(PowahCOE.CABLE_ORB_STARTER_X5_ITEM.get(), type);
        registration.addRecipeCatalyst(PowahCOE.CABLE_ORB_BASIC_X5_ITEM.get(), type);
        registration.addRecipeCatalyst(PowahCOE.CABLE_ORB_HARDENED_X5_ITEM.get(), type);
        registration.addRecipeCatalyst(PowahCOE.CABLE_ORB_BLAZING_X5_ITEM.get(), type);
        registration.addRecipeCatalyst(PowahCOE.CABLE_ORB_NIOTIC_X5_ITEM.get(), type);
        registration.addRecipeCatalyst(PowahCOE.CABLE_ORB_SPIRITED_X5_ITEM.get(), type);
        registration.addRecipeCatalyst(PowahCOE.CABLE_ORB_NITRO_X5_ITEM.get(), type);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(PowahCOE.MODID, "main");
    }
}
