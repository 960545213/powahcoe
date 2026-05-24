package org.PowahCOE.coe.network;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.PowahCOE.coe.PowahCOE;
import org.PowahCOE.coe.block.CableOrbTile;

public record CableOrbSyncPayload(BlockPos pos, CompoundTag data) implements CustomPacketPayload {
    public static final Type<CableOrbSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PowahCOE.MODID, "cable_orb_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CableOrbSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, CableOrbSyncPayload::pos,
                    StreamCodec.of(
                            (RegistryFriendlyByteBuf buf, CompoundTag tag) -> buf.writeNbt(tag),
                            (RegistryFriendlyByteBuf buf) -> {
                                CompoundTag tag = buf.readNbt();
                                return tag != null ? tag : new CompoundTag();
                            }),
                    CableOrbSyncPayload::data,
                    CableOrbSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(CableOrbSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = context.player().level();
            if (level.getBlockEntity(payload.pos) instanceof CableOrbTile tile) {
                tile.handleUpdateTag(payload.data, level.registryAccess());
            }
        });
    }
}
