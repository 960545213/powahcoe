package org.shuashuashua.powahcoe.mixin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import org.slf4j.Logger;
import org.shuashuashua.powahcoe.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import owmii.powah.block.energizing.EnergizingRecipe;
import owmii.powah.lib.logistics.inventory.RecipeWrapper;

@Mixin(EnergizingRecipe.class)
public class MixinEnergizingRecipe {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true, remap = false)
    private void onMatches(RecipeWrapper inv, Level world, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.enableEnergizingStacking) return;

        List<Ingredient> stacks = new ArrayList<>(((EnergizingRecipe) (Object) this).getIngredients());
        boolean matched = true;
        for (int i = 1; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                int needed = stack.getCount();
                Iterator<Ingredient> itr = stacks.iterator();
                while (itr.hasNext() && needed > 0) {
                    Ingredient ingredient = itr.next();
                    if (ingredient.test(stack)) {
                        itr.remove();
                        needed--;
                    }
                }
            }
        }
        if (matched && !stacks.isEmpty()) {
            matched = false;
        }
        cir.setReturnValue(matched);
    }
}
