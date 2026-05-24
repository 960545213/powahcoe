package org.PowahCOE.coe.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.PowahCOE.coe.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import owmii.powah.block.energizing.EnergizingOrbBlock;
import owmii.powah.block.energizing.EnergizingOrbTile;
import owmii.powah.lib.logistics.inventory.Inventory;

@Mixin(EnergizingOrbBlock.class)
public class MixinEnergizingOrbBlock {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true, remap = false)
    private void onUseItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
                              Player player, InteractionHand hand, BlockHitResult hitResult,
                              CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (!Config.enableEnergizingStacking) return;

        BlockEntity tileentity = level.getBlockEntity(pos);
        if (tileentity instanceof EnergizingOrbTile orb) {
            Inventory inv = orb.getInventory();
            ItemStack output = inv.getStackInSlot(0);

            if (held.isEmpty() || !output.isEmpty()) {
                if (!level.isClientSide) {
                    player.getInventory().placeItemBackInInventory(inv.removeNext());
                }
                cir.setReturnValue(ItemInteractionResult.SUCCESS);
            } else {
                if (!level.isClientSide) {
                    ItemStack remaining = held.copy();
                    for (int i = 1; i < inv.getSlots() && !remaining.isEmpty(); i++) {
                        if (orb.canInsert(i, remaining)) {
                            remaining = inv.insertItem(i, remaining, false);
                        }
                    }
                    int inserted = held.getCount() - remaining.getCount();
                    if (inserted > 0 && !player.isCreative()) {
                        held.shrink(inserted);
                    }
                }
                cir.setReturnValue(ItemInteractionResult.SUCCESS);
            }
        }
    }
}
