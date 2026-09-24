package org.shuashuashua.powahcoe.screen;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.shuashuashua.powahcoe.block.CableOrbTile;
import org.shuashuashua.powahcoe.Powahcoe;
import owmii.powah.lib.logistics.inventory.ItemStackHandler;
import owmii.powah.lib.logistics.inventory.slot.SlotItemHandler;

public class CableOrbContainer extends AbstractContainerMenu {

    @Nullable
    private final CableOrbTile tile;
    private int clientAutoEject;
    private static final int TILE_SLOTS = CableOrbTile.INVENTORY_SIZE;

    private static final int[][] OUTPUT_POS = {
        {116, 30}, {134, 30}, {152, 30},
        {116, 48}, {134, 48}, {152, 48},
        {116, 66}, {134, 66}, {152, 66}
    };
    private static final int[][] INPUT_POS = {
        {8, 30}, {26, 30}, {44, 30},
        {8, 48}, {26, 48}, {44, 48},
        {8, 66}, {26, 66}, {44, 66}
    };
    private static final int PLAYER_INV_Y = 136;

    public CableOrbContainer(int containerId, Inventory playerInv, CableOrbTile tile) {
        super(Powahcoe.CABLE_ORB_MENU.get(), containerId);
        this.tile = tile;
        setupSlots(tile.getInventory(), playerInv);
        addDataSlot(createAutoEjectSlot());
    }

    public CableOrbContainer(int containerId, Inventory playerInv) {
        super(Powahcoe.CABLE_ORB_MENU.get(), containerId);
        this.tile = null;
        setupSlots(new ItemStackHandler(TILE_SLOTS), playerInv);
        addDataSlot(createAutoEjectSlot());
    }

    private void setupSlots(ItemStackHandler handler, Inventory playerInv) {
        for (int i = 0; i < CableOrbTile.OUTPUT_END; i++) {
            final int idx = i;
            addSlot(new SlotItemHandler(handler, i, OUTPUT_POS[i][0], OUTPUT_POS[i][1]) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }

        for (int i = 0; i < CableOrbTile.INPUT_END - CableOrbTile.INPUT_START; i++) {
            addSlot(new SlotItemHandler(handler, CableOrbTile.INPUT_START + i, INPUT_POS[i][0], INPUT_POS[i][1]));
        }

        addSlot(new SlotItemHandler(handler, CableOrbTile.UPGRADE_SLOT, 134, 100) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                        new net.minecraft.resources.ResourceLocation("powahcoe", "orb_upgrades")));
            }

            @Override
            public int getMaxStackSize() { return 1; }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, PLAYER_INV_Y + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, PLAYER_INV_Y + 58));
        }
    }

    private DataSlot createAutoEjectSlot() {
        return new DataSlot() {
            @Override
            public int get() {
                return tile != null ? (tile.isAutoEject() ? 1 : 0) : clientAutoEject;
            }

            @Override
            public void set(int value) {
                clientAutoEject = value;
                if (tile != null) {
                    tile.setAutoEject(value != 0);
                }
            }
        };
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return result;

        ItemStack stackInSlot = slot.getItem();
        result = stackInSlot.copy();

        if (index < TILE_SLOTS) {
            if (!this.moveItemStackTo(stackInSlot, TILE_SLOTS, TILE_SLOTS + 36, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (slots.get(CableOrbTile.UPGRADE_SLOT).mayPlace(stackInSlot)) {
                if (!this.moveItemStackTo(stackInSlot, CableOrbTile.UPGRADE_SLOT, TILE_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            } else
            if (!this.moveItemStackTo(stackInSlot, CableOrbTile.INPUT_START, CableOrbTile.INPUT_END, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        if (tile != null) {
            return !tile.isRemoved() && player.level().getBlockEntity(tile.getBlockPos()) == tile
                    && tile.getBlockPos().distToCenterSqr(player.position()) <= 64.0;
        }
        return true;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == 0 && tile != null && !player.level().isClientSide() && stillValid(player)) {
            tile.setAutoEject(!tile.isAutoEject());
            return true;
        }
        return false;
    }

    public boolean isAutoEject() {
        return tile != null ? tile.isAutoEject() : clientAutoEject != 0;
    }
}
