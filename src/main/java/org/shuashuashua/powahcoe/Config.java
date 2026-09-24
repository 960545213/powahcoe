package org.shuashuashua.powahcoe;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.HashMap;
import java.util.Map;

import owmii.powah.block.Tier;

@Mod.EventBusSubscriber(modid = Powahcoe.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue ENABLE_ENERGIZING_STACKING = BUILDER
            .comment(
                "Enable Powah energizing orb input stacking (addon feature)",
                "When enabled, input slots can hold multiple items (up to 64), recipe matching accounts for item count",
                "Requires Powah mod"
            )
            .define("enableEnergizingStacking", true);

    private static final ForgeConfigSpec.LongValue CABLE_ORB_TRANSFER_STARTER = BUILDER
            .comment("Cable Orb max energy receive rate for Starter tier (FE/t)")
            .defineInRange("cableOrbTransferStarter", 100L, 1L, Long.MAX_VALUE);
    private static final ForgeConfigSpec.LongValue CABLE_ORB_TRANSFER_BASIC = BUILDER
            .defineInRange("cableOrbTransferBasic", 400L, 1L, Long.MAX_VALUE);
    private static final ForgeConfigSpec.LongValue CABLE_ORB_TRANSFER_HARDENED = BUILDER
            .defineInRange("cableOrbTransferHardened", 1000L, 1L, Long.MAX_VALUE);
    private static final ForgeConfigSpec.LongValue CABLE_ORB_TRANSFER_BLAZING = BUILDER
            .defineInRange("cableOrbTransferBlazing", 4000L, 1L, Long.MAX_VALUE);
    private static final ForgeConfigSpec.LongValue CABLE_ORB_TRANSFER_NIOTIC = BUILDER
            .defineInRange("cableOrbTransferNiotic", 10000L, 1L, Long.MAX_VALUE);
    private static final ForgeConfigSpec.LongValue CABLE_ORB_TRANSFER_SPIRITED = BUILDER
            .defineInRange("cableOrbTransferSpirited", 40000L, 1L, Long.MAX_VALUE);
    private static final ForgeConfigSpec.LongValue CABLE_ORB_TRANSFER_NITRO = BUILDER
            .defineInRange("cableOrbTransferNitro", 200000L, 1L, Long.MAX_VALUE);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean enableEnergizingStacking;
    public static Map<Tier, Long> cableOrbTransfers = new HashMap<>();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            enableEnergizingStacking = ENABLE_ENERGIZING_STACKING.get();

            cableOrbTransfers = new HashMap<>();
            cableOrbTransfers.put(Tier.STARTER, CABLE_ORB_TRANSFER_STARTER.get());
            cableOrbTransfers.put(Tier.BASIC, CABLE_ORB_TRANSFER_BASIC.get());
            cableOrbTransfers.put(Tier.HARDENED, CABLE_ORB_TRANSFER_HARDENED.get());
            cableOrbTransfers.put(Tier.BLAZING, CABLE_ORB_TRANSFER_BLAZING.get());
            cableOrbTransfers.put(Tier.NIOTIC, CABLE_ORB_TRANSFER_NIOTIC.get());
            cableOrbTransfers.put(Tier.SPIRITED, CABLE_ORB_TRANSFER_SPIRITED.get());
            cableOrbTransfers.put(Tier.NITRO, CABLE_ORB_TRANSFER_NITRO.get());
        }
    }

    public static long getCableOrbTransfer(Tier tier) {
        return cableOrbTransfers.getOrDefault(tier, 100L);
    }
}
