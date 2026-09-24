package org.shuashuashua.powahcoe;

import com.google.common.primitives.Ints;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.shuashuashua.powahcoe.block.CableOrbTile;
import owmii.powah.lib.logistics.energy.Energy;
import owmii.powah.lib.logistics.inventory.Inventory;

public class ForgeSetup {

    public static void init() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(ForgeSetup::clientSetup);
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, ForgeSetup::attachCapabilities);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Screen registration is done in Powahcoe.ClientModEvents
        });
    }

    public static void attachCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof CableOrbTile tile) {
            LazyOptional<net.minecraftforge.energy.IEnergyStorage> energy = LazyOptional.of(tile::getEnergyStorage);
            LazyOptional<net.minecraftforge.items.IItemHandler> inventory =
                    LazyOptional.of(() -> createItemHandler(tile.getInventory()));
            event.addListener(energy::invalidate);
            event.addListener(inventory::invalidate);
            event.addCapability(new ResourceLocation(Powahcoe.MODID, "energy"), new ICapabilityProvider() {
                @NotNull
                @Override
                public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                    if (cap == ForgeCapabilities.ENERGY) {
                        return energy.cast();
                    }
                    return LazyOptional.empty();
                }
            });

            event.addCapability(new ResourceLocation(Powahcoe.MODID, "inventory"), new ICapabilityProvider() {
                @NotNull
                @Override
                public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                    if (cap == ForgeCapabilities.ITEM_HANDLER) {
                        return inventory.cast();
                    }
                    return LazyOptional.empty();
                }
            });
        }
    }

    private static net.minecraftforge.items.IItemHandler createItemHandler(Inventory inv) {
        return new net.minecraftforge.items.IItemHandler() {
            @Override
            public int getSlots() {
                return inv.getSlots();
            }

            @NotNull
            @Override
            public net.minecraft.world.item.ItemStack getStackInSlot(int i) {
                return inv.getStackInSlot(i);
            }

            @NotNull
            @Override
            public net.minecraft.world.item.ItemStack insertItem(int i, @NotNull net.minecraft.world.item.ItemStack arg, boolean bl) {
                return inv.insertItem(i, arg, bl);
            }

            @NotNull
            @Override
            public net.minecraft.world.item.ItemStack extractItem(int i, int j, boolean bl) {
                return inv.extractItem(i, j, bl);
            }

            @Override
            public int getSlotLimit(int i) {
                return inv.getSlotLimit(i);
            }

            @Override
            public boolean isItemValid(int i, @NotNull net.minecraft.world.item.ItemStack arg) {
                return inv.isItemValid(i, arg);
            }
        };
    }
}
