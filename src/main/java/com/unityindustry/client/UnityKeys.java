package com.unityindustry.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.unityindustry.network.UnityActionPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/** X: charge 1 ingot on click, and at most every four ticks while held. */
public final class UnityKeys {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath("unityindustry", "science"));
    private static final KeyMapping CHARGE = KeyBindingHelper.registerKeyBinding(
        new KeyMapping("key.unityindustry.charge", InputConstants.Type.KEYSYM,
            InputConstants.KEY_X, CATEGORY));
    private static final KeyMapping FLY = KeyBindingHelper.registerKeyBinding(
        new KeyMapping("key.unityindustry.fly", InputConstants.Type.KEYSYM,
            InputConstants.KEY_G, CATEGORY));
    private static int heldTicks = 0;

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) { heldTicks = 0; return; }
            if (CHARGE.isDown()) {
                if (heldTicks % 4 == 0 && ClientPlayNetworking.canSend(UnityActionPayload.ID))
                    ClientPlayNetworking.send(new UnityActionPayload(0));
                heldTicks++;
            } else heldTicks = 0;
            while (FLY.consumeClick()) {
                if (ClientPlayNetworking.canSend(UnityActionPayload.ID))
                    ClientPlayNetworking.send(new UnityActionPayload(1));
            }
        });
    }
    private UnityKeys() { }
}
