package org.PowahCOE.coe.mixin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.PowahCOE.coe.Config;
import org.PowahCOE.coe.util.EnergizingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import owmii.powah.block.energizing.EnergizingOrbTile;
import owmii.powah.block.energizing.EnergizingRecipe;
import owmii.powah.recipe.Recipes;
import owmii.powah.lib.logistics.energy.Energy;
import owmii.powah.lib.logistics.inventory.Inventory;

@Mixin(EnergizingOrbTile.class)
public class MixinEnergizingOrbTile {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Shadow(remap = false)
    @Nullable
    private Energy buffer;

    @Shadow(remap = false)
    @Nullable
    private RecipeHolder<EnergizingRecipe> recipe;

    @Shadow(remap = false)
    private boolean containRecipe;

    @Inject(method = "getSlotLimit", at = @At("HEAD"), cancellable = true, remap = false)
    private void onGetSlotLimit(int index, CallbackInfoReturnable<Integer> cir) {
        if (Config.enableEnergizingStacking) {
            cir.setReturnValue(64);
        }
    }

    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true, remap = false)
    private void onCanInsert(int index, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.enableEnergizingStacking) return;
        if (index == 0) {
            cir.setReturnValue(false);
            return;
        }
        Inventory inv = ((EnergizingOrbTile) (Object) this).getInventory();
        if (!inv.getStackInSlot(0).isEmpty()) {
            cir.setReturnValue(false);
            return;
        }
        ItemStack existing = inv.getStackInSlot(index);
        if (existing.isEmpty()) {
            cir.setReturnValue(true);
            return;
        }
        cir.setReturnValue(ItemStack.isSameItemSameComponents(existing, stack)
                && existing.getCount() < existing.getMaxStackSize());
    }

    @Inject(method = "fillEnergy", at = @At("HEAD"), cancellable = true, remap = false)
    private void onFillEnergy(long amount, CallbackInfoReturnable<Long> cir) {
        if (!Config.enableEnergizingStacking) return;

        EnergizingOrbTile self = (EnergizingOrbTile) (Object) this;
        long filled = Math.min(this.buffer.getEmpty(), amount);
        if (self.getLevel() != null) {
            if (this.recipe != null) {
                this.buffer.produce(filled);
                if (this.buffer.isFull()) {
                    ItemStack result = this.recipe.value().getResultItem(self.getLevel().registryAccess());
                    Inventory inv = self.getInventory();
                    consumeIngredients(inv);

                    ItemStack outputSlot = inv.getStackInSlot(0);
                    if (outputSlot.isEmpty()) {
                        inv.setStackInSlot(0, result.copy());
                    } else if (ItemStack.isSameItemSameComponents(outputSlot, result)
                            && outputSlot.getCount() + result.getCount() <= outputSlot.getMaxStackSize()) {
                        outputSlot.grow(result.getCount());
                    }

                    self.onSlotChanged(0);
                    self.setChanged();

                    // Reset buffer and re-check recipe for next craft cycle
                    this.buffer.setStored(0);
                    checkRecipe(self);
                }
                self.sync(5);
            }
        }
        cir.setReturnValue(filled);
    }

    private void checkRecipe(EnergizingOrbTile self) {
        if (!Config.enableEnergizingStacking) return;

        Level level = self.getLevel();
        if (level == null || level.isClientSide()) return;

        Inventory inv = self.getInventory();

        // Block crafting while output slot has items (wait for player to extract)
        if (!inv.getStackInSlot(0).isEmpty()) {
            if (self.containRecipe()) {
                this.recipe = null;
                this.buffer.setCapacity(0);
                this.buffer.setStored(0);
                this.buffer.setTransfer(0);
                self.setContainRecipe(false);
                self.sync(1);
            }
            return;
        }

        java.util.List<RecipeHolder<EnergizingRecipe>> recipes =
                level.getRecipeManager().getAllRecipesFor(Recipes.ENERGIZING.get());

        for (RecipeHolder<EnergizingRecipe> holder : recipes) {
            if (stackingMatches(holder.value(), inv, 1, inv.getSlots())) {
                this.recipe = holder;
                this.buffer.setCapacity(holder.value().getScaledEnergy());
                this.buffer.setTransfer(holder.value().getScaledEnergy());
                self.setContainRecipe(true);
                self.sync(1);
                return;
            }
        }

        this.recipe = null;
        this.buffer.setCapacity(0);
        this.buffer.setStored(0);
        this.buffer.setTransfer(0);
        self.setContainRecipe(false);
        self.sync(1);
    }

    private void consumeIngredients(Inventory inv) {
        if (this.recipe == null) return;
        EnergizingHelper.consumeIngredients(this.recipe, inv, 1, 1, inv.getSlots());
    }

    @Inject(method = "checkRecipe", at = @At("HEAD"), cancellable = true, remap = false)
    private void onCheckRecipe(CallbackInfo ci) {
        if (!Config.enableEnergizingStacking) return;
        EnergizingOrbTile self = (EnergizingOrbTile) (Object) this;
        checkRecipe(self);
        ci.cancel();
    }

    private static boolean stackingMatches(EnergizingRecipe recipe, Inventory inv, int firstInputSlot, int lastInputSlot) {
        return EnergizingHelper.stackingMatches(recipe, inv, firstInputSlot, lastInputSlot);
    }
}
