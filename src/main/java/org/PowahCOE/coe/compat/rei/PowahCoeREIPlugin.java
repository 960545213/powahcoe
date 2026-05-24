package org.PowahCOE.coe.compat.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import org.PowahCOE.coe.PowahCOE;
import owmii.powah.compat.rei.energizing.EnergizingCategory;

public class PowahCoeREIPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        var id = EnergizingCategory.ID;
        registry.addWorkstations(id, EntryStacks.of(PowahCOE.CABLE_ORB_STARTER_ITEM.get()));
        registry.addWorkstations(id, EntryStacks.of(PowahCOE.CABLE_ORB_BASIC_ITEM.get()));
        registry.addWorkstations(id, EntryStacks.of(PowahCOE.CABLE_ORB_HARDENED_ITEM.get()));
        registry.addWorkstations(id, EntryStacks.of(PowahCOE.CABLE_ORB_BLAZING_ITEM.get()));
        registry.addWorkstations(id, EntryStacks.of(PowahCOE.CABLE_ORB_NIOTIC_ITEM.get()));
        registry.addWorkstations(id, EntryStacks.of(PowahCOE.CABLE_ORB_SPIRITED_ITEM.get()));
        registry.addWorkstations(id, EntryStacks.of(PowahCOE.CABLE_ORB_NITRO_ITEM.get()));
        registry.addWorkstations(id, EntryStacks.of(PowahCOE.CABLE_ORB_STARTER_X5_ITEM.get()));
        registry.addWorkstations(id, EntryStacks.of(PowahCOE.CABLE_ORB_BASIC_X5_ITEM.get()));
        registry.addWorkstations(id, EntryStacks.of(PowahCOE.CABLE_ORB_HARDENED_X5_ITEM.get()));
        registry.addWorkstations(id, EntryStacks.of(PowahCOE.CABLE_ORB_BLAZING_X5_ITEM.get()));
        registry.addWorkstations(id, EntryStacks.of(PowahCOE.CABLE_ORB_NIOTIC_X5_ITEM.get()));
        registry.addWorkstations(id, EntryStacks.of(PowahCOE.CABLE_ORB_SPIRITED_X5_ITEM.get()));
        registry.addWorkstations(id, EntryStacks.of(PowahCOE.CABLE_ORB_NITRO_X5_ITEM.get()));
    }
}
