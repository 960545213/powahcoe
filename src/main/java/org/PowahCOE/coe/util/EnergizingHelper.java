package org.PowahCOE.coe.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import owmii.powah.block.energizing.EnergizingRecipe;
import owmii.powah.lib.logistics.inventory.Inventory;

public final class EnergizingHelper {

    private EnergizingHelper() {}

    /** Check if input slots [firstInputSlot, lastInputSlot) satisfy the recipe with stacking. */
    public static boolean stackingMatches(EnergizingRecipe recipe, Inventory inv, int firstInputSlot, int lastInputSlot) {
        List<Ingredient> stacks = new ArrayList<>(recipe.getIngredients());
        for (int i = firstInputSlot; i < lastInputSlot; i++) {
            ItemStack stack = inv.getStackInSlot(i);
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
        return stacks.isEmpty();
    }

    /** Map upgrade item to its batch multiplier (1-6). Returns 1 for non-upgrade items. */
    public static int getUpgradeMultiplier(ItemStack stack) {
        if (stack.isEmpty()) return 1;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!"powahcoe".equals(id.getNamespace())) return 1;
        String path = id.getPath();
        if (path.startsWith("batch_upgrade_")) {
            try {
                int n = Integer.parseInt(path.substring("batch_upgrade_".length()));
                return n; // 1,2,3,4,5,6
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return 1;
    }

    /**
     * Returns the number of batches possible given the upgrade slot item and
     * available ingredient sets. Returns at least 1 (no upgrade = single batch).
     * With upgrade: multiplier is the number of full stacks to aim for.
     */
    public static int getBatchCount(RecipeHolder<EnergizingRecipe> recipe, Inventory inv,
                                     int firstInputSlot, int lastInputSlot, int upgradeSlot,
                                     int resultCount, int resultMaxStack) {
        ItemStack upgradeStack = inv.getStackInSlot(upgradeSlot);
        if (upgradeStack.isEmpty()) return 1;
        int multiplier = getUpgradeMultiplier(upgradeStack);
        int batchesPerStack = (resultMaxStack + resultCount - 1) / resultCount; // ceiling
        int maxBatches = multiplier * batchesPerStack;

        List<Ingredient> ingredients = recipe.value().getIngredients();
        if (ingredients.isEmpty()) return Math.max(1, maxBatches);

        int inputCount = lastInputSlot - firstInputSlot;
        List<Ingredient> perBatch = new ArrayList<>(ingredients);
        int batches = 0;
        int[] slotCounts = new int[inputCount];
        for (int i = 0; i < inputCount; i++) {
            slotCounts[i] = inv.getStackInSlot(firstInputSlot + i).getCount();
        }
        ItemStack[] slotStacks = new ItemStack[inputCount];
        for (int i = 0; i < inputCount; i++) {
            slotStacks[i] = inv.getStackInSlot(firstInputSlot + i);
        }

        while (batches < maxBatches) {
            List<Ingredient> remaining = new ArrayList<>(perBatch);
            int[] used = new int[slotCounts.length];
            for (int i = 0; i < slotCounts.length; i++) {
                int avail = slotCounts[i] - used[i];
                if (avail <= 0 || slotStacks[i].isEmpty()) continue;
                Iterator<Ingredient> itr = remaining.iterator();
                while (itr.hasNext() && used[i] < slotCounts[i]) {
                    if (itr.next().test(slotStacks[i])) {
                        itr.remove();
                        used[i]++;
                    }
                }
            }
            if (remaining.isEmpty()) {
                batches++;
                for (int i = 0; i < slotCounts.length; i++) {
                    slotCounts[i] -= used[i];
                }
            } else {
                break;
            }
        }
        return Math.max(1, batches);
    }

    /** Consume recipe ingredients {@code batchCount} times from input slots [firstInputSlot, lastInputSlot). */
    public static void consumeIngredients(RecipeHolder<EnergizingRecipe> recipe, Inventory inv,
                                           int batchCount, int firstInputSlot, int lastInputSlot) {
        List<Ingredient> remaining = new ArrayList<>();
        for (int b = 0; b < batchCount; b++) {
            remaining.addAll(recipe.value().getIngredients());
        }
        for (int i = firstInputSlot; i < lastInputSlot; i++) {
            ItemStack slotStack = inv.getStackInSlot(i);
            if (slotStack.isEmpty()) continue;
            int canRemove = 0;
            for (Ingredient ingredient : remaining) {
                if (canRemove >= slotStack.getCount()) break;
                if (ingredient.test(slotStack)) canRemove++;
            }
            if (canRemove == 0) continue;
            Iterator<Ingredient> itr = remaining.iterator();
            int removed = 0;
            while (itr.hasNext() && removed < canRemove) {
                if (itr.next().test(slotStack)) {
                    itr.remove();
                    removed++;
                }
            }
            slotStack.shrink(removed);
        }
    }
}
