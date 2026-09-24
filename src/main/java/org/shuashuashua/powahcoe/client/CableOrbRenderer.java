package org.shuashuashua.powahcoe.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.shuashuashua.powahcoe.Powahcoe;
import org.shuashuashua.powahcoe.block.CableOrbTile;
import owmii.powah.lib.client.util.RenderTypes;
import owmii.powah.lib.logistics.energy.Energy;
import owmii.powah.lib.util.math.V3d;

import java.util.ArrayList;
import java.util.List;

public class CableOrbRenderer implements BlockEntityRenderer<CableOrbTile> {
    private static final ResourceLocation ORB_TEXTURE =
            new ResourceLocation("powah", "textures/model/tile/energy_charge.png");

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation("powahcoe", "cable_orb"), "cable_orb");

    private final ModelPart orbCube;

    public CableOrbRenderer(BlockEntityRendererProvider.Context context) {
        this.orbCube = context.bakeLayer(LAYER).getChild("cube");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("cube",
                CubeListBuilder.create().addBox(-2.5F, -2.5F, -2.5F, 5, 5, 5),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 20, 10);
    }

    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(LAYER, CableOrbRenderer::createLayer);
    }

    public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Powahcoe.CABLE_ORB_TILE.get(), CableOrbRenderer::new);
    }

    @Override
    public void render(CableOrbTile te, float pt, PoseStack matrix, MultiBufferSource rtb,
                       int light, int ov) {
        Minecraft mc = Minecraft.getInstance();
        var world = mc.level;
        var player = mc.player;
        if (world == null || player == null) return;

        var inv = te.getInventory();
        Direction up = te.getOrbUp();
        double x = 0.5 + up.getStepX() * 0.1;
        double y = 0.5 + up.getStepY() * 0.1;
        double z = 0.5 + up.getStepZ() * 0.1;

        float ticks = (world.getGameTime() + pt) / 200.0F;

        if (!inv.isEmpty()) {
            ItemStack output = inv.getStackInSlot(0);
            if (!output.isEmpty()) {
                matrix.pushPose();
                matrix.translate(x, y, z);
                matrix.mulPose(Axis.YP.rotationDegrees(-ticks * 360.0F));
                matrix.scale(0.35F, 0.35F, 0.35F);
                mc.getItemRenderer().renderStatic(output, ItemDisplayContext.FIXED, light, ov, matrix, rtb, world, 0);
                matrix.popPose();
            } else {
                List<ItemStack> stacks = new ArrayList<>(inv.getNonEmptyStacks());
                List<V3d> circled = V3d.from(Vec3.ZERO).circled(stacks.size(), 0.12D);
                for (int i = 0; i < circled.size(); i++) {
                    V3d v3d1 = circled.get(i);
                    ItemStack stack = stacks.get(i);
                    if (!stack.isEmpty()) {
                        matrix.pushPose();
                        if (stacks.size() == 1) {
                            matrix.translate(x, y, z);
                        } else {
                            matrix.translate(v3d1.x + x, v3d1.y + y, v3d1.z + z);
                        }
                        matrix.scale(0.35F, 0.35F, 0.35F);
                        matrix.mulPose(Axis.YP.rotationDegrees(-ticks * 360.0F));
                        mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, ov, matrix, rtb, world, 0);
                        matrix.popPose();
                    }
                }
            }
        }

        matrix.pushPose();
        matrix.translate(0.5D, 0.5D, 0.5D);
        matrix.mulPose(up.getRotation());
        matrix.translate(0.0D, 0.1D, 0.0D);
        matrix.scale(1.8F, 1.8F, 1.8F);

        int color = -1;
        if (te.containRecipe()) {
            Energy buffer = te.getBuffer();
            long capacity = buffer.getMaxEnergyStored();
            if (capacity > 0) {
                float progress = (float) buffer.getEnergyStored() / (float) capacity;
                int alpha = (int) (38 + progress * 217);
                color = (alpha << 24) | (0x33 << 16) | (0x99 << 8) | 0xFF;
            }
        }

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;
        this.orbCube.render(matrix, rtb.getBuffer(RenderTypes.entityBlendedNoDept(ORB_TEXTURE)),
                light, ov, r, g, b, a);
        matrix.popPose();
    }
}
