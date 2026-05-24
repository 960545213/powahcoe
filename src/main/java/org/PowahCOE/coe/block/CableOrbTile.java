package org.PowahCOE.coe.block;

import java.util.Optional;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import org.PowahCOE.coe.network.CableOrbSyncPayload;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.PowahCOE.coe.Config;
import org.PowahCOE.coe.util.EnergizingHelper;

import owmii.powah.block.Tier;
import owmii.powah.block.energizing.EnergizingRecipe;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import owmii.powah.lib.logistics.energy.Energy;
import owmii.powah.lib.logistics.inventory.Inventory;
import owmii.powah.recipe.Recipes;

public class CableOrbTile extends BlockEntity implements owmii.powah.lib.block.IInventoryHolder {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final int OUTPUT_START = 0;
    public static final int OUTPUT_END = 9;
    public static final int INPUT_START = 9;
    public static final int INPUT_END = 18;
    public static final int UPGRADE_SLOT = 18;
    private static final int INVENTORY_SIZE = 19;
    private static final int SYNC_COOLDOWN = 20; // ticks between full inventory syncs

    // Cached tag for upgrade slot validation — avoids allocating TagKey each insert
    private static final TagKey<net.minecraft.world.item.Item> UPGRADE_TAG =
            TagKey.create(Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath("powahcoe", "orb_upgrades"));

    private final Inventory inv;
    private final Energy buffer;
    private final CableOrbInput recipeInput;

    @Nullable
    private RecipeHolder<EnergizingRecipe> recipe;
    private boolean containRecipe;
    private boolean autoEject;

    public CableOrbTile(BlockPos pos, BlockState state) {
        super(org.PowahCOE.coe.PowahCOE.CABLE_ORB_TILE.get(), pos, state);
        this.inv = new Inventory(INVENTORY_SIZE);
        this.inv.setTile(this);
        this.recipeInput = new CableOrbInput(inv, INPUT_START, INPUT_END);
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
            if (!inv.isEmpty()) {
                syncToClient();
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        invalidateCapabilities();
    }

    private static long getTierMaxReceive(BlockState state) {
        if (state.getBlock() instanceof CableOrbBlock block) {
            return Config.getCableOrbTransfer(block.getVariant()) * block.getMultiplier();
        }
        return 100;
    }

    private long getCurrentMaxReceive() {
        long base = getTierMaxReceive(getBlockState());
        ItemStack upgrade = inv.getStackInSlot(UPGRADE_SLOT);
        return base * EnergizingHelper.getUpgradeMultiplier(upgrade);
    }

public Tier getVariant() {
        if (getBlockState().getBlock() instanceof CableOrbBlock block) {
            return block.getVariant();
        }
        return Tier.STARTER;
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
        // Input slots: allow if any output slot is empty
        if (!anyOutputSlotEmpty()) return false;
        ItemStack existing = inv.getStackInSlot(index);
        if (existing.isEmpty()) return true;
        return ItemStack.isSameItemSameComponents(existing, stack)
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

    private int getOutputBatchesCapacity(ItemStack result) {
        int maxStack = result.getMaxStackSize();
        int resultCount = result.getCount();
        int batchesPerSlot = maxStack / resultCount;
        int total = 0;
        for (int i = OUTPUT_START; i < OUTPUT_END; i++) {
            ItemStack slot = inv.getStackInSlot(i);
            if (slot.isEmpty()) {
                total += batchesPerSlot;
            } else if (ItemStack.isSameItemSameComponents(slot, result)) {
                int space = maxStack - slot.getCount();
                total += space / resultCount;
            }
        }
        return total;
    }

    private int lastSyncTick;
    private boolean needsSync;
    private int craftCooldown; // matches Powah rod's 20-tick energy transfer rhythm

    private void syncToClient() {
        syncToClient(false);
    }

    private void syncToClient(boolean force) {
        if (this.level instanceof ServerLevel serverLevel) {
            int currentTick = serverLevel.getServer().getTickCount();
            if (!force && currentTick - lastSyncTick < SYNC_COOLDOWN) {
                needsSync = true;
                return;
            }
            lastSyncTick = currentTick;
            needsSync = false;
            setChanged();
            CompoundTag tag = saveWithoutMetadata(serverLevel.registryAccess());
            CableOrbSyncPayload payload = new CableOrbSyncPayload(this.worldPosition, tag);
            PacketDistributor.sendToPlayersTrackingChunk(
                    serverLevel, new ChunkPos(this.worldPosition), payload);
        }
    }

    private void syncEnergy() {
        if (this.level instanceof ServerLevel serverLevel) {
            CompoundTag tag = new CompoundTag();
            this.buffer.write(tag, "buffer", true, false);
            tag.putBoolean("contain_recipe", this.containRecipe);
            CableOrbSyncPayload payload = new CableOrbSyncPayload(this.worldPosition, tag);
            PacketDistributor.sendToPlayersTrackingChunk(
                    serverLevel, new ChunkPos(this.worldPosition), payload);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
    }

    @Override
    public void onSlotChanged(int index) {
        if (level != null && !level.isClientSide()) {
            if (!crafting) {
                checkRecipe();
            }
        }
    }

    /**
     * RecipeInput adapter that presents our input slots to the recipe system.
     * Powah's EnergizingRecipe.matches() skips index 0 (output) and checks 1..size()-1.
     * We map: index 0 = empty placeholder, index 1..6 = our input slots 6..11.
     */
    record CableOrbInput(Inventory inventory, int firstInputSlot, int lastInputSlot) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            if (index == 0) return ItemStack.EMPTY;
            int mappedIndex = firstInputSlot + index - 1;
            if (mappedIndex < lastInputSlot) {
                return inventory.getStackInSlot(mappedIndex).copy();
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return 1 + (lastInputSlot - firstInputSlot);
        }
    }

    private boolean outputHasSpace() {
        for (int i = OUTPUT_START; i < OUTPUT_END; i++) {
            ItemStack slot = inv.getStackInSlot(i);
            if (slot.isEmpty() || slot.getCount() < slot.getMaxStackSize()) return true;
        }
        return false;
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

        // Skip expensive getRecipeFor if no ingredients present
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

        // Use vanilla getRecipeFor — respects the recipe's own matches() method,
        // so KubeJS and other mod-added recipes work correctly.
        Optional<RecipeHolder<EnergizingRecipe>> found = level.getRecipeManager()
                .getRecipeFor(Recipes.ENERGIZING.get(), recipeInput, level);

        if (found.isPresent()) {
            RecipeHolder<EnergizingRecipe> holder = found.get();
            ItemStack resultSample = holder.value().getResultItem(level.registryAccess());
            int maxOutputBatches = getOutputBatchesCapacity(resultSample);
            if (maxOutputBatches <= 0) {
                this.recipe = null;
                this.buffer.setCapacity(0);
                this.buffer.setStored(0);
                this.buffer.setMaxExtract(0);
                this.remainingBatches = 0;
                this.containRecipe = false;
                setChanged();
                return;
            }
            this.recipe = holder;
            this.remainingBatches = EnergizingHelper.getBatchCount(holder, inv, INPUT_START, INPUT_END, UPGRADE_SLOT,
                    resultSample.getCount(), resultSample.getMaxStackSize());
            if (this.remainingBatches > maxOutputBatches) {
                this.remainingBatches = maxOutputBatches;
            }
            long totalEnergy = holder.value().getScaledEnergy() * this.remainingBatches;
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
    private int syncTimer;
    private boolean crafting;

    public long fillEnergy(long amount) {
        if (level == null || this.recipe == null) return 0;
        if (craftCooldown > 0) return 0;
        long filled = Math.min(this.buffer.getEmpty(), Math.min(amount, getCurrentMaxReceive()));
        if (filled <= 0) return 0;
        this.buffer.produce(filled);
        syncTimer++;
        if (syncTimer >= 20) {
            syncTimer = 0;
            syncEnergy();
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
            ItemStack result = this.recipe.value().getResultItem(level.registryAccess());
            EnergizingHelper.consumeIngredients(this.recipe, inv, this.remainingBatches, INPUT_START, INPUT_END);
            distributeOutput(result);
            this.crafting = false;
            craftCooldown = 20;
            checkRecipe();
            syncToClient();
        }
        return filled;
    }

    private void distributeOutput(ItemStack result) {
        int remaining = result.getCount() * this.remainingBatches;
        int maxStack = result.getMaxStackSize();

        // First try to stack into existing matching output slots
        for (int i = OUTPUT_START; i < OUTPUT_END && remaining > 0; i++) {
            ItemStack slot = inv.getStackInSlot(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, result)) {
                int space = maxStack - slot.getCount();
                if (space > 0) {
                    int add = Math.min(remaining, space);
                    slot.grow(add);
                    remaining -= add;
                }
            }
        }

        // Then fill empty slots
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
    public RecipeHolder<EnergizingRecipe> currRecipe() {
        return recipe;
    }

    public boolean containRecipe() {
        return containRecipe;
    }

    public boolean isAutoEject() {
        return autoEject;
    }

    public void setAutoEject(boolean autoEject) {
        this.autoEject = autoEject;
        setChanged();
        syncToClient(true);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CableOrbTile tile) {
        if (level.isClientSide()) return;
        tile.autoEjectTick(level, pos);
        if (tile.craftCooldown > 0) tile.craftCooldown--;
        // Flush deferred sync after SYNC_COOLDOWN passes
        if (tile.needsSync && level.getServer().getTickCount() - tile.lastSyncTick >= SYNC_COOLDOWN) {
            tile.syncToClient();
        }
    }

    private void autoEjectTick(Level level, BlockPos pos) {
        if (!autoEject) return;

        for (int slotIdx = OUTPUT_START; slotIdx < OUTPUT_END; slotIdx++) {
            ItemStack output = inv.getStackInSlot(slotIdx);
            if (output.isEmpty()) continue;

            for (Direction dir : Direction.values()) {
                BlockPos targetPos = pos.relative(dir);
                IItemHandler handler = level.getCapability(
                        Capabilities.ItemHandler.BLOCK, targetPos, dir.getOpposite());
                if (handler == null) continue;

                ItemStack remaining = output.copy();
                for (int i = 0; i < handler.getSlots() && !remaining.isEmpty(); i++) {
                    remaining = handler.insertItem(i, remaining, false);
                }
                inv.setStackInSlot(slotIdx, remaining);
                setChanged();
                if (remaining.isEmpty()) break;
            }

            if (!inv.getStackInSlot(slotIdx).isEmpty()) break;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.buffer.write(tag, "buffer", true, false);
        tag.putBoolean("contain_recipe", this.containRecipe);
        tag.putBoolean("auto_eject", this.autoEject);
        tag.merge(this.inv.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.buffer.read(tag, "buffer", true, false);
        this.buffer.setMaxExtract(this.buffer.getCapacity());
        this.containRecipe = tag.getBoolean("contain_recipe");
        this.autoEject = tag.getBoolean("auto_eject");
        if (tag.contains("Items")) {
            this.inv.deserializeNBT(tag, registries);
        }
    }

    private final net.neoforged.neoforge.energy.IEnergyStorage energyStorage = new EnergyStorageAdapter();

    public net.neoforged.neoforge.energy.IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    private class EnergyStorageAdapter implements net.neoforged.neoforge.energy.IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (recipe == null) return 0;
            long maxReceiveL = maxReceive;
            long empty = buffer.getEmpty();
            long accepted = Math.min(empty, Math.min(getCurrentMaxReceive(), maxReceiveL));
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
