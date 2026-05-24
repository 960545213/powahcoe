package org.PowahCOE.coe;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slf4j.Logger;

@EventBusSubscriber(modid = PowahCOE.MODID, value = Dist.CLIENT)
public class PowahCOEClient {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        LOGGER.info("[powahcoe] Registering CableOrbRenderer layer definition");
        event.registerLayerDefinition(
                org.PowahCOE.coe.client.CableOrbRenderer.LAYER,
                org.PowahCOE.coe.client.CableOrbRenderer::createLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        LOGGER.info("[powahcoe] Registering CableOrbRenderer for CABLE_ORB_TILE");
        event.registerBlockEntityRenderer(
                PowahCOE.CABLE_ORB_TILE.get(),
                org.PowahCOE.coe.client.CableOrbRenderer::new);
    }
}
