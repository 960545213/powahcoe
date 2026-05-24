package org.PowahCOE.coe;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.Map;

import owmii.powah.block.Tier;

@EventBusSubscriber(modid = PowahCOE.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLE_ENERGIZING_STACKING = BUILDER
            .comment(
                "是否启用Powah充能台输入槽堆叠功能（附属功能）",
                "开启后充能台的输入槽可以放入多个相同物品（最多64个），配方匹配会将物品数量计入",
                "需要Powah mod作为前置",
                "Enable Powah energizing orb input stacking (addon feature)",
                "When enabled, input slots can hold multiple items (up to 64), recipe matching accounts for item count",
                "Requires Powah mod"
            )
            .define("enableEnergizingStacking", true);

    private static final ModConfigSpec.LongValue CABLE_ORB_TRANSFER_STARTER = BUILDER
            .comment("Cable Orb max energy receive rate for Starter tier (FE/t)")
            .defineInRange("cableOrbTransferStarter", 100, 1, Long.MAX_VALUE);
    private static final ModConfigSpec.LongValue CABLE_ORB_TRANSFER_BASIC = BUILDER
            .defineInRange("cableOrbTransferBasic", 400, 1, Long.MAX_VALUE);
    private static final ModConfigSpec.LongValue CABLE_ORB_TRANSFER_HARDENED = BUILDER
            .defineInRange("cableOrbTransferHardened", 1000, 1, Long.MAX_VALUE);
    private static final ModConfigSpec.LongValue CABLE_ORB_TRANSFER_BLAZING = BUILDER
            .defineInRange("cableOrbTransferBlazing", 4000, 1, Long.MAX_VALUE);
    private static final ModConfigSpec.LongValue CABLE_ORB_TRANSFER_NIOTIC = BUILDER
            .defineInRange("cableOrbTransferNiotic", 10000, 1, Long.MAX_VALUE);
    private static final ModConfigSpec.LongValue CABLE_ORB_TRANSFER_SPIRITED = BUILDER
            .defineInRange("cableOrbTransferSpirited", 40000, 1, Long.MAX_VALUE);
    private static final ModConfigSpec.LongValue CABLE_ORB_TRANSFER_NITRO = BUILDER
            .defineInRange("cableOrbTransferNitro", 200000, 1, Long.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean enableEnergizingStacking;
    public static Map<Tier, Long> cableOrbTransfers = new HashMap<>();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
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

    public static long getCableOrbTransfer(Tier tier) {
        return cableOrbTransfers.getOrDefault(tier, 100L);
    }
}
