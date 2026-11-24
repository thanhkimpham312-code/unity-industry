package com.unityindustry.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Bounded server-to-client ore coordinates; blank payload clears highlights. */
public record OreScanPayload(String packedPositions) implements CustomPacketPayload {
    public static final Type<OreScanPayload> ID =
        new Type<>(Identifier.fromNamespaceAndPath("unityindustry", "ore_scan"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OreScanPayload> CODEC =
        StreamCodec.composite(ByteBufCodecs.STRING_UTF8, OreScanPayload::packedPositions, OreScanPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return ID; }
}
