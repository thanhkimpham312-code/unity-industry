package com.unityindustry.network;

import com.unityindustry.gameplay.UnityEnergy;
import com.unityindustry.gameplay.UnityPowers;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class UnityNetworking {
    public static void initialize() {
        PayloadTypeRegistry.playC2S().register(UnityActionPayload.ID, UnityActionPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(UnityActionPayload.ID, (payload, context) -> {
            // Payload is not trusted; every case must be checked on server.
            if (payload.action() == 0) UnityEnergy.chargeOne(context.player());
            if (payload.action() == 1) UnityPowers.toggleFlight(context.player());
            if (payload.action() >= 10 && payload.action() <= 18)
                UnityPowers.handleSkillKey(context.player(), payload.action() - 9);
            if (payload.action() >= 20 && payload.action() <= 28)
                UnityPowers.setHeldSkill(context.player(), payload.action() - 19, true);
            if (payload.action() >= 30 && payload.action() <= 38)
                UnityPowers.setHeldSkill(context.player(), payload.action() - 29, false);
        });
    }
    private UnityNetworking() { }
}
