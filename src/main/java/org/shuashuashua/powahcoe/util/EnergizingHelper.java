package org.shuashuashua.powahcoe.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import owmii.powah.block.energizing.EnergizingRecipe;
import owmii.powah.lib.logistics.inventory.Inventory;

public final class EnergizingHelper {

    private EnergizingHelper() {}

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

    public static int getUpgradeMultiplier(ItemStack stack) {
        if (stack.isEmpty()) return 1;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!"powahcoe".equals(id.getNamespace())) return 1;
        String path = id.getPath();
        if (path.startsWith("batch_upgrade_")) {
            try {
                int n = Integer.parseInt(path.substring("batch_upgrade_".length()));
                return OrbRules.upgradeLevel(n);
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return 1;
    }

    public static int getBatchCount(EnergizingRecipe recipe, Inventory inv,
                                     int firstInputSlot, int lastInputSlot, int upgradeSlot) {
        ItemStack upgradeStack = inv.getStackInSlot(upgradeSlot);
        if (upgradeStack.isEmpty()) return 1;
        int multiplier = getUpgradeMultiplier(upgradeStack);
        ItemStack result = recipe.getResultItem();
        int maxBatches = OrbRules.targetBatches(true, multiplier, result.getCount(), result.getMaxStackSize());

        List<Ingredient> ingredients = recipe.getIngredients();
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

    public static void consumeIngredients(EnergizingRecipe recipe, Inventory inv,
                                           int batchCount, int firstInputSlot, int lastInputSlot) {
        List<Ingredient> remaining = new ArrayList<>();
        for (int b = 0; b < batchCount; b++) {
            remaining.addAll(recipe.getIngredients());
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
