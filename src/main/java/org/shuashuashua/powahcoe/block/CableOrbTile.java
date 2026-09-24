package org.shuashuashua.powahcoe.block;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.shuashuashua.powahcoe.Config;
import org.shuashuashua.powahcoe.Powahcoe;
import org.shuashuashua.powahcoe.util.EnergizingHelper;
import org.shuashuashua.powahcoe.util.OrbRules;
import org.shuashuashua.powahcoe.util.OrbInventoryMigration;

import owmii.powah.block.Tier;
import owmii.powah.block.energizing.EnergizingRecipe;
import owmii.powah.lib.logistics.energy.Energy;
import owmii.powah.lib.logistics.inventory.Inventory;
import owmii.powah.lib.logistics.inventory.RecipeWrapper;
import owmii.powah.recipe.Recipes;

import java.util.Optional;

public class CableOrbTile extends BlockEntity implements owmii.powah.lib.block.IInventoryHolder {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final int OUTPUT_START = 0;
    public static final int OUTPUT_END = OrbRules.OUTPUT_END;
    public static final int INPUT_START = OrbRules.INPUT_START;
    public static final int INPUT_END = OrbRules.INPUT_END;
    public static final int UPGRADE_SLOT = OrbRules.UPGRADE_SLOT;
    public static final int INVENTORY_SIZE = OrbRules.INVENTORY_SIZE;
    private static final int SYNC_COOLDOWN = 20;
    private long lastSyncTick = -SYNC_COOLDOWN;
    private boolean needsSync;
    private int syncTimer;

    private static final TagKey<net.minecraft.world.item.Item> UPGRADE_TAG =
            TagKey.create(Registries.ITEM,
                    new ResourceLocation("powahcoe", "orb_upgrades"));

    private final Inventory inv;
    private final Energy buffer;

    @Nullable
    private EnergizingRecipe recipe;
    private boolean containRecipe;
    private boolean autoEject;

    public CableOrbTile(BlockPos pos, BlockState state) {
        super(Powahcoe.CABLE_ORB_TILE.get(), pos, state);
        this.inv = new Inventory(INVENTORY_SIZE);
        this.inv.setTile(this);
        long maxReceive = getTierMaxReceive(state);
        this.buffer = Energy.create(0);
        this.buffer.setMaxReceive(maxReceive);
        this.buffer.setCapacity(0);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            checkRecipe();
            syncToClient(true);
        }
    }

    private static long getTierMaxReceive(BlockState state) {
        if (state.getBlock() instanceof CableOrbBlock block) {
            return OrbRules.multiplyEnergy(Config.getCableOrbTransfer(block.getVariant()), block.getMultiplier());
        }
        return 100;
    }

    public Tier getVariant() {
        if (getBlockState().getBlock() instanceof CableOrbBlock block) {
            return block.getVariant();
        }
        return Tier.STARTER;
    }

    private long getCurrentMaxReceive() {
        return OrbRules.multiplyEnergy(getTierMaxReceive(getBlockState()),
                EnergizingHelper.getUpgradeMultiplier(inv.getStackInSlot(UPGRADE_SLOT)));
    }

    private void syncToClient(boolean force) {
        if (level == null || level.isClientSide()) return;
        setChanged();
        long now = level.getGameTime();
        if (!force && now - lastSyncTick < SYNC_COOLDOWN) {
            needsSync = true;
            return;
        }
        needsSync = false;
        lastSyncTick = now;
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }

    public Direction getOrbUp() {
        if (this.level != null) {
            var state = this.getBlockState();
            if (state.hasProperty(BlockStateProperties.FACING)) {
                return state.getValue(BlockStateProperties.FACING).getOpposite();
            }
        }
        return Direction.UP;
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

    @Override
    public int getSlotLimit(int index) {
        return index == UPGRADE_SLOT ? 1 : 64;
    }

    @Override
    public boolean canInsert(int index, ItemStack stack) {
        if (index < OUTPUT_END) return false;
        if (index == UPGRADE_SLOT) {
            return stack.is(UPGRADE_TAG);
        }
        if (!anyOutputSlotEmpty()) return false;
        ItemStack existing = inv.getStackInSlot(index);
        if (existing.isEmpty()) return true;
        return ItemStack.isSameItemSameTags(existing, stack)
                && existing.getCount() < existing.getMaxStackSize();
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack) {
        return true;
    }

    private boolean anyOutputSlotEmpty() {
        for (int i = OUTPUT_START; i < OUTPUT_END; i++) {
            if (inv.getStackInSlot(i).isEmpty()) return true;
        }
        return false;
    }

    private boolean outputHasSpace() {
        for (int i = OUTPUT_START; i < OUTPUT_END; i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) return true;
        }
        return false;
    }

    private int getOutputBatchesCapacity(ItemStack result) {
        if (result.isEmpty()) return 0;
        int total = 0;
        for (int i = OUTPUT_START; i < OUTPUT_END; i++) {
            ItemStack stack = inv.getStackInSlot(i);
            total += OrbRules.outputBatchesInSlot(result.getCount(), result.getMaxStackSize(),
                    stack.getCount(), stack.isEmpty() || ItemStack.isSameItemSameTags(stack, result));
        }
        return total;
    }

    @Override
    public void onSlotChanged(int index) {
        if (level != null && !level.isClientSide()) {
            if (!crafting) {
                checkRecipe();
                syncToClient(false);
            }
        }
    }

    private void checkRecipe() {
        if (level == null || level.isClientSide()) return;

        if (!outputHasSpace()) {
            if (this.containRecipe) {
                this.recipe = null;
                this.buffer.setCapacity(0);
                this.buffer.setStored(0);
                this.buffer.setMaxExtract(0);
                this.remainingBatches = 0;
                this.containRecipe = false;
                setChanged();
            }
            return;
        }

        boolean hasInput = false;
        for (int i = INPUT_START; i < INPUT_END; i++) {
            if (!inv.getStackInSlot(i).isEmpty()) { hasInput = true; break; }
        }
        if (!hasInput) {
            if (this.containRecipe) {
                this.recipe = null;
                this.buffer.setCapacity(0);
                this.buffer.setStored(0);
                this.buffer.setMaxExtract(0);
                this.remainingBatches = 0;
                this.containRecipe = false;
                setChanged();
            }
            return;
        }

        // Powah and our stacking mixin skip slot zero (the output placeholder).
        // Expose all nine inputs at indices 1..9, never real output/upgrade slots.
        RecipeWrapper wrapper = new RecipeWrapper(inv) {
            @Override
            public int getContainerSize() {
                return 1 + INPUT_END - INPUT_START;
            }

            @Override
            public net.minecraft.world.item.ItemStack getItem(int slot) {
                return slot == 0 ? ItemStack.EMPTY : inv.getStackInSlot(INPUT_START + slot - 1).copy();
            }
        };
        Optional<EnergizingRecipe> found = level.getRecipeManager()
                .getRecipeFor(Recipes.ENERGIZING.get(), wrapper, level);

        if (found.isPresent()) {
            EnergizingRecipe rec = found.get();
            this.recipe = rec;
            this.remainingBatches = EnergizingHelper.getBatchCount(rec, inv, INPUT_START, INPUT_END, UPGRADE_SLOT);
            ItemStack resultSample = rec.getResultItem(level.registryAccess());
            int maxBatchesByOutput = getOutputBatchesCapacity(resultSample);
            if (this.remainingBatches > maxBatchesByOutput) {
                this.remainingBatches = maxBatchesByOutput;
            }
            if (this.remainingBatches <= 0) {
                this.recipe = null;
                this.buffer.setCapacity(0);
                this.buffer.setStored(0);
                this.buffer.setMaxExtract(0);
                this.containRecipe = false;
                setChanged();
                return;
            }
            long totalEnergy = OrbRules.multiplyEnergy(rec.getEnergy(), this.remainingBatches);
            this.buffer.setCapacity(totalEnergy);
            this.buffer.setStored(0);
            this.buffer.setMaxExtract(totalEnergy);
            this.containRecipe = true;
            setChanged();
        } else {
            this.recipe = null;
            this.buffer.setCapacity(0);
            this.buffer.setStored(0);
            this.buffer.setMaxExtract(0);
            this.remainingBatches = 0;
            this.containRecipe = false;
            setChanged();
        }
    }

    private int remainingBatches;
    private int craftCooldown;
    private boolean crafting;

    public long fillEnergy(long amount) {
        if (level == null || this.recipe == null) return 0;
        if (craftCooldown > 0) return 0;
        long filled = Math.min(this.buffer.getEmpty(), Math.min(amount, getCurrentMaxReceive()));
        if (filled <= 0) return 0;
        this.buffer.produce(filled);
        setChanged();
        if (++syncTimer >= 20) {
            syncTimer = 0;
            syncToClient(false);
            if (level instanceof ServerLevel serverLevel) {
                BlockPos pos = this.worldPosition;
                double px = pos.getX() + 0.3 + level.random.nextDouble() * 0.4;
                double py = pos.getY() + 0.5;
                double pz = pos.getZ() + 0.3 + level.random.nextDouble() * 0.4;
                serverLevel.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK, px, py, pz,
                        1, 0.0, 0.0, 0.0, 0.05);
            }
        }
        if (this.buffer.isFull()) {
            this.crafting = true;
            ItemStack result = this.recipe.getResultItem(level.registryAccess());
            EnergizingHelper.consumeIngredients(this.recipe, inv, this.remainingBatches, INPUT_START, INPUT_END);
            distributeOutput(result);
            this.crafting = false;
            craftCooldown = 20;
            checkRecipe();
            syncToClient(false);
        }
        return filled;
    }

    private void distributeOutput(ItemStack result) {
        int remaining = result.getCount() * this.remainingBatches;
        int maxStack = result.getMaxStackSize();

        for (int i = OUTPUT_START; i < OUTPUT_END && remaining > 0; i++) {
            ItemStack slot = inv.getStackInSlot(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameTags(slot, result)) {
                int space = maxStack - slot.getCount();
                if (space > 0) {
                    int add = Math.min(remaining, space);
                    slot.grow(add);
                    remaining -= add;
                }
            }
        }

        for (int i = OUTPUT_START; i < OUTPUT_END && remaining > 0; i++) {
            ItemStack slot = inv.getStackInSlot(i);
            if (slot.isEmpty()) {
                int add = Math.min(remaining, maxStack);
                ItemStack out = result.copy();
                out.setCount(add);
                inv.setStackInSlot(i, out);
                remaining -= add;
            }
        }
    }

    public Energy getBuffer() {
        return buffer;
    }

    @Nullable
    public EnergizingRecipe currRecipe() {
        return recipe;
    }

    public boolean containRecipe() {
        return containRecipe;
    }

    public boolean isAutoEject() {
        return autoEject;
    }

    public void setAutoEject(boolean autoEject) {
        if (this.autoEject == autoEject) return;
        this.autoEject = autoEject;
        syncToClient(true);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CableOrbTile tile) {
        if (level.isClientSide()) return;
        tile.autoEjectTick(level, pos);
        if (tile.craftCooldown > 0) tile.craftCooldown--;
        if (tile.needsSync && level.getGameTime() - tile.lastSyncTick >= SYNC_COOLDOWN) {
            tile.syncToClient(false);
        }
    }

    private void autoEjectTick(Level level, BlockPos pos) {
        if (!autoEject) return;
        boolean changed = false;
        crafting = true;
        try {
        for (int slotIdx = OUTPUT_START; slotIdx < OUTPUT_END; slotIdx++) {
            ItemStack output = inv.getStackInSlot(slotIdx);
            if (output.isEmpty()) continue;
            ItemStack remaining = output.copy();
            for (Direction dir : Direction.values()) {
                BlockPos targetPos = pos.relative(dir);
                BlockEntity targetTile = level.getBlockEntity(targetPos);
                if (targetTile == null) continue;
                IItemHandler handler = targetTile.getCapability(
                        ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).orElse(null);
                if (handler == null) continue;

                for (int i = 0; i < handler.getSlots() && !remaining.isEmpty(); i++) {
                    remaining = handler.insertItem(i, remaining, false);
                }
                if (remaining.isEmpty()) break;
            }
            if (remaining.getCount() != output.getCount()) {
                inv.setStackInSlot(slotIdx, remaining);
                changed = true;
            }
            if (!remaining.isEmpty()) break;
        }
        } finally {
            crafting = false;
        }
        if (changed) {
            checkRecipe();
            syncToClient(false);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.buffer.write(tag, "buffer", true, false);
        tag.putBoolean("contain_recipe", this.containRecipe);
        tag.putBoolean("auto_eject", this.autoEject);
        tag.merge(this.inv.serializeNBT());
        tag.putInt(OrbInventoryMigration.VERSION_KEY, 1);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.buffer.read(tag, "buffer", true, false);
        this.buffer.setMaxExtract(this.buffer.getCapacity());
        this.containRecipe = tag.getBoolean("contain_recipe");
        this.autoEject = tag.getBoolean("auto_eject");
        if (tag.contains("Items")) {
            this.inv.deserializeNBT(OrbInventoryMigration.upgrade(tag));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        // Network updates must not treat omitted fields as false/empty disk data.
        if (tag.contains("energy_capacity_buffer")) {
            this.buffer.setCapacity(tag.getLong("energy_capacity_buffer"));
            this.buffer.setMaxExtract(this.buffer.getCapacity());
        }
        if (tag.contains("energy_stored_buffer")) this.buffer.setStored(tag.getLong("energy_stored_buffer"));
        if (tag.contains("contain_recipe")) this.containRecipe = tag.getBoolean("contain_recipe");
        if (tag.contains("auto_eject")) this.autoEject = tag.getBoolean("auto_eject");
        if (tag.contains("Items")) this.inv.deserializeNBT(OrbInventoryMigration.upgrade(tag));
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) handleUpdateTag(packet.getTag());
    }

    private final net.minecraftforge.energy.IEnergyStorage energyStorage = new EnergyStorageAdapter();
    public net.minecraftforge.energy.IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    private class EnergyStorageAdapter implements net.minecraftforge.energy.IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (recipe == null) return 0;
            long empty = buffer.getEmpty();
            long accepted = Math.min(empty, Math.min(getCurrentMaxReceive(), maxReceive));
            if (accepted <= 0) return 0;
            if (!simulate) {
                return (int) fillEnergy(accepted);
            }
            return (int) accepted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, buffer.getEnergyStored());
        }

        @Override
        public int getMaxEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, buffer.getMaxEnergyStored());
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}
