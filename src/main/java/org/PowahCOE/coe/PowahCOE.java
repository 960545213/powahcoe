package org.PowahCOE.coe;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import org.PowahCOE.coe.block.CableOrbBlock;
import org.PowahCOE.coe.block.CableOrbTile;
import org.PowahCOE.coe.network.CableOrbSyncPayload;

import owmii.powah.block.Tier;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.TooltipFlag;

@Mod(PowahCOE.MODID)
public class PowahCOE {
    public static final String MODID = "powahcoe";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Cable Energizing Orb blocks (7 tiers)
    public static final DeferredBlock<CableOrbBlock> CABLE_ORB_STARTER = BLOCKS.register("cable_orb_starter",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.STARTER));
    public static final DeferredBlock<CableOrbBlock> CABLE_ORB_BASIC = BLOCKS.register("cable_orb_basic",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.BASIC));
    public static final DeferredBlock<CableOrbBlock> CABLE_ORB_HARDENED = BLOCKS.register("cable_orb_hardened",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.HARDENED));
    public static final DeferredBlock<CableOrbBlock> CABLE_ORB_BLAZING = BLOCKS.register("cable_orb_blazing",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.BLAZING));
    public static final DeferredBlock<CableOrbBlock> CABLE_ORB_NIOTIC = BLOCKS.register("cable_orb_niotic",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.NIOTIC));
    public static final DeferredBlock<CableOrbBlock> CABLE_ORB_SPIRITED = BLOCKS.register("cable_orb_spirited",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.SPIRITED));
    public static final DeferredBlock<CableOrbBlock> CABLE_ORB_NITRO = BLOCKS.register("cable_orb_nitro",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.NITRO));

    // Cable Energizing Orb blocks — 5x variants
    public static final DeferredBlock<CableOrbBlock> CABLE_ORB_STARTER_X5 = BLOCKS.register("cable_orb_starter_x5",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.STARTER, 5));
    public static final DeferredBlock<CableOrbBlock> CABLE_ORB_BASIC_X5 = BLOCKS.register("cable_orb_basic_x5",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.BASIC, 5));
    public static final DeferredBlock<CableOrbBlock> CABLE_ORB_HARDENED_X5 = BLOCKS.register("cable_orb_hardened_x5",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.HARDENED, 5));
    public static final DeferredBlock<CableOrbBlock> CABLE_ORB_BLAZING_X5 = BLOCKS.register("cable_orb_blazing_x5",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.BLAZING, 5));
    public static final DeferredBlock<CableOrbBlock> CABLE_ORB_NIOTIC_X5 = BLOCKS.register("cable_orb_niotic_x5",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.NIOTIC, 5));
    public static final DeferredBlock<CableOrbBlock> CABLE_ORB_SPIRITED_X5 = BLOCKS.register("cable_orb_spirited_x5",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.SPIRITED, 5));
    public static final DeferredBlock<CableOrbBlock> CABLE_ORB_NITRO_X5 = BLOCKS.register("cable_orb_nitro_x5",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.NITRO, 5));

    // Cable Energizing Orb items
    public static final DeferredItem<BlockItem> CABLE_ORB_STARTER_ITEM = ITEMS.registerSimpleBlockItem("cable_orb_starter", CABLE_ORB_STARTER);
    public static final DeferredItem<BlockItem> CABLE_ORB_BASIC_ITEM = ITEMS.registerSimpleBlockItem("cable_orb_basic", CABLE_ORB_BASIC);
    public static final DeferredItem<BlockItem> CABLE_ORB_HARDENED_ITEM = ITEMS.registerSimpleBlockItem("cable_orb_hardened", CABLE_ORB_HARDENED);
    public static final DeferredItem<BlockItem> CABLE_ORB_BLAZING_ITEM = ITEMS.registerSimpleBlockItem("cable_orb_blazing", CABLE_ORB_BLAZING);
    public static final DeferredItem<BlockItem> CABLE_ORB_NIOTIC_ITEM = ITEMS.registerSimpleBlockItem("cable_orb_niotic", CABLE_ORB_NIOTIC);
    public static final DeferredItem<BlockItem> CABLE_ORB_SPIRITED_ITEM = ITEMS.registerSimpleBlockItem("cable_orb_spirited", CABLE_ORB_SPIRITED);
    public static final DeferredItem<BlockItem> CABLE_ORB_NITRO_ITEM = ITEMS.registerSimpleBlockItem("cable_orb_nitro", CABLE_ORB_NITRO);
    // 5x items
    public static final DeferredItem<BlockItem> CABLE_ORB_STARTER_X5_ITEM = ITEMS.registerSimpleBlockItem("cable_orb_starter_x5", CABLE_ORB_STARTER_X5);
    public static final DeferredItem<BlockItem> CABLE_ORB_BASIC_X5_ITEM = ITEMS.registerSimpleBlockItem("cable_orb_basic_x5", CABLE_ORB_BASIC_X5);
    public static final DeferredItem<BlockItem> CABLE_ORB_HARDENED_X5_ITEM = ITEMS.registerSimpleBlockItem("cable_orb_hardened_x5", CABLE_ORB_HARDENED_X5);
    public static final DeferredItem<BlockItem> CABLE_ORB_BLAZING_X5_ITEM = ITEMS.registerSimpleBlockItem("cable_orb_blazing_x5", CABLE_ORB_BLAZING_X5);
    public static final DeferredItem<BlockItem> CABLE_ORB_NIOTIC_X5_ITEM = ITEMS.registerSimpleBlockItem("cable_orb_niotic_x5", CABLE_ORB_NIOTIC_X5);
    public static final DeferredItem<BlockItem> CABLE_ORB_SPIRITED_X5_ITEM = ITEMS.registerSimpleBlockItem("cable_orb_spirited_x5", CABLE_ORB_SPIRITED_X5);
    public static final DeferredItem<BlockItem> CABLE_ORB_NITRO_X5_ITEM = ITEMS.registerSimpleBlockItem("cable_orb_nitro_x5", CABLE_ORB_NITRO_X5);

    // Batch upgrade items (x1 to x7) — with enchantment glint
    public static final DeferredItem<Item> BATCH_UPGRADE_1 = ITEMS.registerItem("batch_upgrade_1", props -> batchUpgradeItem(1, props));
    public static final DeferredItem<Item> BATCH_UPGRADE_2 = ITEMS.registerItem("batch_upgrade_2", props -> batchUpgradeItem(2, props));
    public static final DeferredItem<Item> BATCH_UPGRADE_3 = ITEMS.registerItem("batch_upgrade_3", props -> batchUpgradeItem(3, props));
    public static final DeferredItem<Item> BATCH_UPGRADE_4 = ITEMS.registerItem("batch_upgrade_4", props -> batchUpgradeItem(4, props));
    public static final DeferredItem<Item> BATCH_UPGRADE_5 = ITEMS.registerItem("batch_upgrade_5", props -> batchUpgradeItem(5, props));
    public static final DeferredItem<Item> BATCH_UPGRADE_6 = ITEMS.registerItem("batch_upgrade_6", props -> batchUpgradeItem(6, props));
    public static final DeferredItem<Item> BATCH_UPGRADE_7 = ITEMS.registerItem("batch_upgrade_7", props -> batchUpgradeItem(7, props));

    // Single BlockEntityType for all cable orb blocks
    public static final Supplier<BlockEntityType<CableOrbTile>> CABLE_ORB_TILE = TILES.register("cable_orb",
            () -> new BlockEntityType<>(CableOrbTile::new, Set.of(
                    CABLE_ORB_STARTER.get(), CABLE_ORB_BASIC.get(), CABLE_ORB_HARDENED.get(),
                    CABLE_ORB_BLAZING.get(), CABLE_ORB_NIOTIC.get(), CABLE_ORB_SPIRITED.get(),
                    CABLE_ORB_NITRO.get(),
                    CABLE_ORB_STARTER_X5.get(), CABLE_ORB_BASIC_X5.get(), CABLE_ORB_HARDENED_X5.get(),
                    CABLE_ORB_BLAZING_X5.get(), CABLE_ORB_NIOTIC_X5.get(), CABLE_ORB_SPIRITED_X5.get(),
                    CABLE_ORB_NITRO_X5.get()
            ), null));

    // Creative tab: More Energizing Orbs
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CABLE_ORB_TAB = CREATIVE_MODE_TABS.register("cable_orb_tab",
            () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.powahcoe"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> CABLE_ORB_STARTER_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(CABLE_ORB_STARTER_ITEM.get());
                        output.accept(CABLE_ORB_BASIC_ITEM.get());
                        output.accept(CABLE_ORB_HARDENED_ITEM.get());
                        output.accept(CABLE_ORB_BLAZING_ITEM.get());
                        output.accept(CABLE_ORB_NIOTIC_ITEM.get());
                        output.accept(CABLE_ORB_SPIRITED_ITEM.get());
                        output.accept(CABLE_ORB_NITRO_ITEM.get());
                        output.accept(CABLE_ORB_STARTER_X5_ITEM.get());
                        output.accept(CABLE_ORB_BASIC_X5_ITEM.get());
                        output.accept(CABLE_ORB_HARDENED_X5_ITEM.get());
                        output.accept(CABLE_ORB_BLAZING_X5_ITEM.get());
                        output.accept(CABLE_ORB_NIOTIC_X5_ITEM.get());
                        output.accept(CABLE_ORB_SPIRITED_X5_ITEM.get());
                        output.accept(CABLE_ORB_NITRO_X5_ITEM.get());
                        output.accept(BATCH_UPGRADE_1.get());
                        output.accept(BATCH_UPGRADE_2.get());
                        output.accept(BATCH_UPGRADE_3.get());
                        output.accept(BATCH_UPGRADE_4.get());
                        output.accept(BATCH_UPGRADE_5.get());
                        output.accept(BATCH_UPGRADE_6.get());
                        output.accept(BATCH_UPGRADE_7.get());
                    }).build());

    public PowahCOE(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        TILES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(RegisterCapabilitiesEvent.class, this::registerCapabilities);
        modEventBus.addListener(RegisterPayloadHandlersEvent.class, this::registerPayloads);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(MODID).playToClient(
                CableOrbSyncPayload.TYPE,
                CableOrbSyncPayload.STREAM_CODEC,
                CableOrbSyncPayload::handleClient);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        BlockEntityType<CableOrbTile> type = CABLE_ORB_TILE.get();
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type,
                (tile, side) -> tile.getEnergyStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type,
                (tile, side) -> tile.getInventory());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    private static Item batchUpgradeItem(int multiplier, Item.Properties props) {
        return new Item(props) {
            @Override
            public boolean isFoil(ItemStack stack) {
                return true;
            }

            @Override
            public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                tooltip.add(Component.translatable("tooltip.powahcoe.batch_upgrade", multiplier)
                        .withStyle(ChatFormatting.GRAY));
            }
        };
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getGameProfile().getName());
        }
    }
}
