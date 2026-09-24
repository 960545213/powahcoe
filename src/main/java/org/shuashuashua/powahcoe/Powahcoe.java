package org.shuashuashua.powahcoe;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.shuashuashua.powahcoe.block.CableOrbBlock;
import org.shuashuashua.powahcoe.block.CableOrbTile;
import org.shuashuashua.powahcoe.screen.CableOrbContainer;
import org.slf4j.Logger;

import owmii.powah.block.Tier;

import java.util.Set;

@Mod(Powahcoe.MODID)
public class Powahcoe {
    public static final String MODID = "powahcoe";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Cable Energizing Orb blocks (7 tiers)
    public static final RegistryObject<CableOrbBlock> CABLE_ORB_STARTER = BLOCKS.register("cable_orb_starter",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.STARTER));
    public static final RegistryObject<CableOrbBlock> CABLE_ORB_BASIC = BLOCKS.register("cable_orb_basic",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.BASIC));
    public static final RegistryObject<CableOrbBlock> CABLE_ORB_HARDENED = BLOCKS.register("cable_orb_hardened",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.HARDENED));
    public static final RegistryObject<CableOrbBlock> CABLE_ORB_BLAZING = BLOCKS.register("cable_orb_blazing",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.BLAZING));
    public static final RegistryObject<CableOrbBlock> CABLE_ORB_NIOTIC = BLOCKS.register("cable_orb_niotic",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.NIOTIC));
    public static final RegistryObject<CableOrbBlock> CABLE_ORB_SPIRITED = BLOCKS.register("cable_orb_spirited",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.SPIRITED));
    public static final RegistryObject<CableOrbBlock> CABLE_ORB_NITRO = BLOCKS.register("cable_orb_nitro",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.NITRO));

    // Cable Energizing Orb blocks - x5 variants
    public static final RegistryObject<CableOrbBlock> CABLE_ORB_STARTER_X5 = BLOCKS.register("cable_orb_starter_x5",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.STARTER, 5));
    public static final RegistryObject<CableOrbBlock> CABLE_ORB_BASIC_X5 = BLOCKS.register("cable_orb_basic_x5",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.BASIC, 5));
    public static final RegistryObject<CableOrbBlock> CABLE_ORB_HARDENED_X5 = BLOCKS.register("cable_orb_hardened_x5",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.HARDENED, 5));
    public static final RegistryObject<CableOrbBlock> CABLE_ORB_BLAZING_X5 = BLOCKS.register("cable_orb_blazing_x5",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.BLAZING, 5));
    public static final RegistryObject<CableOrbBlock> CABLE_ORB_NIOTIC_X5 = BLOCKS.register("cable_orb_niotic_x5",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.NIOTIC, 5));
    public static final RegistryObject<CableOrbBlock> CABLE_ORB_SPIRITED_X5 = BLOCKS.register("cable_orb_spirited_x5",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.SPIRITED, 5));
    public static final RegistryObject<CableOrbBlock> CABLE_ORB_NITRO_X5 = BLOCKS.register("cable_orb_nitro_x5",
            () -> new CableOrbBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F, 20.0F).noOcclusion().requiresCorrectToolForDrops(), Tier.NITRO, 5));

    // Block items
    public static final RegistryObject<BlockItem> CABLE_ORB_STARTER_ITEM = ITEMS.register("cable_orb_starter", () -> new BlockItem(CABLE_ORB_STARTER.get(), defaultProps()));
    public static final RegistryObject<BlockItem> CABLE_ORB_BASIC_ITEM = ITEMS.register("cable_orb_basic", () -> new BlockItem(CABLE_ORB_BASIC.get(), defaultProps()));
    public static final RegistryObject<BlockItem> CABLE_ORB_HARDENED_ITEM = ITEMS.register("cable_orb_hardened", () -> new BlockItem(CABLE_ORB_HARDENED.get(), defaultProps()));
    public static final RegistryObject<BlockItem> CABLE_ORB_BLAZING_ITEM = ITEMS.register("cable_orb_blazing", () -> new BlockItem(CABLE_ORB_BLAZING.get(), defaultProps()));
    public static final RegistryObject<BlockItem> CABLE_ORB_NIOTIC_ITEM = ITEMS.register("cable_orb_niotic", () -> new BlockItem(CABLE_ORB_NIOTIC.get(), defaultProps()));
    public static final RegistryObject<BlockItem> CABLE_ORB_SPIRITED_ITEM = ITEMS.register("cable_orb_spirited", () -> new BlockItem(CABLE_ORB_SPIRITED.get(), defaultProps()));
    public static final RegistryObject<BlockItem> CABLE_ORB_NITRO_ITEM = ITEMS.register("cable_orb_nitro", () -> new BlockItem(CABLE_ORB_NITRO.get(), defaultProps()));
    // 5x items
    public static final RegistryObject<BlockItem> CABLE_ORB_STARTER_X5_ITEM = ITEMS.register("cable_orb_starter_x5", () -> new BlockItem(CABLE_ORB_STARTER_X5.get(), defaultProps()));
    public static final RegistryObject<BlockItem> CABLE_ORB_BASIC_X5_ITEM = ITEMS.register("cable_orb_basic_x5", () -> new BlockItem(CABLE_ORB_BASIC_X5.get(), defaultProps()));
    public static final RegistryObject<BlockItem> CABLE_ORB_HARDENED_X5_ITEM = ITEMS.register("cable_orb_hardened_x5", () -> new BlockItem(CABLE_ORB_HARDENED_X5.get(), defaultProps()));
    public static final RegistryObject<BlockItem> CABLE_ORB_BLAZING_X5_ITEM = ITEMS.register("cable_orb_blazing_x5", () -> new BlockItem(CABLE_ORB_BLAZING_X5.get(), defaultProps()));
    public static final RegistryObject<BlockItem> CABLE_ORB_NIOTIC_X5_ITEM = ITEMS.register("cable_orb_niotic_x5", () -> new BlockItem(CABLE_ORB_NIOTIC_X5.get(), defaultProps()));
    public static final RegistryObject<BlockItem> CABLE_ORB_SPIRITED_X5_ITEM = ITEMS.register("cable_orb_spirited_x5", () -> new BlockItem(CABLE_ORB_SPIRITED_X5.get(), defaultProps()));
    public static final RegistryObject<BlockItem> CABLE_ORB_NITRO_X5_ITEM = ITEMS.register("cable_orb_nitro_x5", () -> new BlockItem(CABLE_ORB_NITRO_X5.get(), defaultProps()));

    // Batch upgrade items
    public static final RegistryObject<Item> BATCH_UPGRADE_1 = ITEMS.register("batch_upgrade_1", () -> glintItem(defaultProps()));
    public static final RegistryObject<Item> BATCH_UPGRADE_2 = ITEMS.register("batch_upgrade_2", () -> glintItem(defaultProps()));
    public static final RegistryObject<Item> BATCH_UPGRADE_3 = ITEMS.register("batch_upgrade_3", () -> glintItem(defaultProps()));
    public static final RegistryObject<Item> BATCH_UPGRADE_4 = ITEMS.register("batch_upgrade_4", () -> glintItem(defaultProps()));
    public static final RegistryObject<Item> BATCH_UPGRADE_5 = ITEMS.register("batch_upgrade_5", () -> glintItem(defaultProps()));
    public static final RegistryObject<Item> BATCH_UPGRADE_6 = ITEMS.register("batch_upgrade_6", () -> glintItem(defaultProps()));
    public static final RegistryObject<Item> BATCH_UPGRADE_7 = ITEMS.register("batch_upgrade_7", () -> glintItem(defaultProps()));

    // BlockEntityType for all cable orb blocks
    @SuppressWarnings("unchecked")
    public static final RegistryObject<BlockEntityType<CableOrbTile>> CABLE_ORB_TILE = TILES.register("cable_orb",
            () -> new BlockEntityType<>(CableOrbTile::new, Set.of(
                    CABLE_ORB_STARTER.get(), CABLE_ORB_BASIC.get(), CABLE_ORB_HARDENED.get(),
                    CABLE_ORB_BLAZING.get(), CABLE_ORB_NIOTIC.get(), CABLE_ORB_SPIRITED.get(),
                    CABLE_ORB_NITRO.get(),
                    CABLE_ORB_STARTER_X5.get(), CABLE_ORB_BASIC_X5.get(), CABLE_ORB_HARDENED_X5.get(),
                    CABLE_ORB_BLAZING_X5.get(), CABLE_ORB_NIOTIC_X5.get(), CABLE_ORB_SPIRITED_X5.get(),
                    CABLE_ORB_NITRO_X5.get()
            ), null));

    // Menu type
    public static final RegistryObject<MenuType<CableOrbContainer>> CABLE_ORB_MENU = MENUS.register("cable_orb",
            () -> new MenuType<>(CableOrbContainer::new, net.minecraft.world.flag.FeatureFlags.VANILLA_SET));

    // Creative tab
    public static final RegistryObject<CreativeModeTab> CABLE_ORB_TAB = CREATIVE_MODE_TABS.register("cable_orb_tab",
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

    public Powahcoe() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        TILES.register(modEventBus);
        MENUS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        ForgeSetup.init();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("PowahCOE common setup");
    }

    @SubscribeEvent
    public void onServerStarting(net.minecraftforge.event.server.ServerStartingEvent event) {
        LOGGER.info("PowahCOE server starting");
    }

    private static Item glintItem(Item.Properties props) {
        return new Item(props) {
            @Override
            public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level,
                                        java.util.List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
                super.appendHoverText(stack, level, tooltip, flag);
                tooltip.add(Component.translatable("tooltip.powahcoe.batch_upgrade",
                        org.shuashuashua.powahcoe.util.EnergizingHelper.getUpgradeMultiplier(stack))
                        .withStyle(net.minecraft.ChatFormatting.GRAY));
            }

            @Override
            public boolean isFoil(ItemStack stack) {
                return true;
            }
        };
    }

    private static Item.Properties defaultProps() {
        return new Item.Properties();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                net.minecraft.client.gui.screens.MenuScreens.register(CABLE_ORB_MENU.get(), org.shuashuashua.powahcoe.screen.CableOrbScreen::new);
            });
        }

        @SubscribeEvent
        public static void onRegisterRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(CABLE_ORB_TILE.get(), org.shuashuashua.powahcoe.client.CableOrbRenderer::new);
        }

        @SubscribeEvent
        public static void onRegisterLayerDefinitions(net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions event) {
            org.shuashuashua.powahcoe.client.CableOrbRenderer.registerLayer(event);
        }
    }
}
