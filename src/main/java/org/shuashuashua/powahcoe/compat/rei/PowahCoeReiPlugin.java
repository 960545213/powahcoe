package org.shuashuashua.powahcoe.compat.rei;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.network.chat.Component;
import org.shuashuashua.powahcoe.Powahcoe;
import owmii.powah.block.energizing.EnergizingRecipe;
import owmii.powah.recipe.Recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Discovered only by REI on the client, never loaded by our common/server code. */
@REIPluginClient
public final class PowahCoeReiPlugin implements REIClientPlugin {
    private static final CategoryIdentifier<OrbDisplay> CATEGORY = CategoryIdentifier.of("powahcoe", "energizing");

    @Override
    public void registerCategories(CategoryRegistry registry) {
        // Powah 5.0.11 has no native REI category; provide one for its recipes.
        registry.add(new OrbCategory());
        Powahcoe.BLOCKS.getEntries().forEach(block -> registry.addWorkstations(CATEGORY, EntryStacks.of(block.get())));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerRecipeFiller(EnergizingRecipe.class, Recipes.ENERGIZING.get(), OrbDisplay::new);
    }

    public static final class OrbDisplay extends BasicDisplay {
        private final long energy;

        OrbDisplay(EnergizingRecipe recipe) {
            super(recipe.getIngredients().stream().map(EntryIngredients::ofIngredient).toList(),
                    List.of(EntryIngredients.of(recipe.getResultItem())), Optional.of(recipe.getId()));
            energy = recipe.getEnergy();
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() { return CATEGORY; }
    }

    private static final class OrbCategory implements DisplayCategory<OrbDisplay> {
        @Override
        public CategoryIdentifier<? extends OrbDisplay> getCategoryIdentifier() { return CATEGORY; }

        @Override
        public Component getTitle() { return Component.translatable("category.powahcoe.energizing"); }

        @Override
        public Renderer getIcon() { return EntryStacks.of(Powahcoe.CABLE_ORB_STARTER_ITEM.get()); }

        @Override
        public int getDisplayHeight() { return 86; }

        @Override
        public List<Widget> setupDisplay(OrbDisplay display, Rectangle bounds) {
            List<Widget> widgets = new ArrayList<>();
            widgets.add(Widgets.createRecipeBase(bounds));
            for (int i = 0; i < display.getInputEntries().size(); i++) {
                widgets.add(Widgets.createSlot(new Point(bounds.x + 8 + i % 3 * 18, bounds.y + 8 + i / 3 * 18))
                        .entries(display.getInputEntries().get(i)).markInput());
            }
            widgets.add(Widgets.createArrow(new Point(bounds.x + 76, bounds.y + 27)));
            widgets.add(Widgets.createSlot(new Point(bounds.x + 117, bounds.y + 27))
                    .entries(display.getOutputEntries().get(0)).markOutput());
            widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), bounds.y + 69),
                    Component.literal(String.format(java.util.Locale.ROOT, "%,d FE", display.energy))));
            return widgets;
        }
    }
}
