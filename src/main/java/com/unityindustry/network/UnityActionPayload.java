package com.unityindustry.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** action: 0 charge, 1 flight toggle. Server validates all requests. */
public record UnityActionPayload(int action) implements CustomPacketPayload {
    public static final Type<UnityActionPayload> ID =
        new Type<>(Identifier.fromNamespaceAndPath("unityindustry", "action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UnityActionPayload> CODEC =
        StreamCodec.composite(ByteBufCodecs.VAR_INT, UnityActionPayload::action, UnityActionPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return ID; }
}
