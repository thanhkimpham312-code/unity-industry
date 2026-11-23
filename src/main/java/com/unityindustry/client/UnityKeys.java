package com.unityindustry.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.unityindustry.network.UnityActionPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/** Configurable keyboard controls. Slot 2 currently activates Flying/Ultra flight.
 * Other slot actions are reserved and transparently report they are unfinished.
 */
public final class UnityKeys {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath("unityindustry", "science"));
    private static KeyMapping mapping(String id, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.unityindustry." + id, InputConstants.Type.KEYSYM, key, CATEGORY));
    }
    private static final KeyMapping CHARGE = mapping("charge", InputConstants.KEY_X);
    private static final KeyMapping FLY = mapping("fly", InputConstants.KEY_G);
    private static final KeyMapping[] SKILLS = new KeyMapping[9];
    private static final int[] NUMBERS = {
        InputConstants.KEY_1, InputConstants.KEY_2, InputConstants.KEY_3,
        InputConstants.KEY_4, InputConstants.KEY_5, InputConstants.KEY_6,
        InputConstants.KEY_7, InputConstants.KEY_8, InputConstants.KEY_9
    };
    static {
        for (int i = 0; i < SKILLS.length; i++) SKILLS[i] = mapping("skill_" + (i + 1), NUMBERS[i]);
    }
    private static int heldTicks;
    private static void send(int action) {
        if (ClientPlayNetworking.canSend(UnityActionPayload.ID))
            ClientPlayNetworking.send(new UnityActionPayload(action));
    }
    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) { heldTicks = 0; return; }
            if (client.screen != null) { heldTicks = 0; return; }
            if (CHARGE.isDown()) {
                if (heldTicks % 4 == 0) send(0); // 1 ingot per 0.2 second
                heldTicks++;
            } else heldTicks = 0;
            while (FLY.consumeClick()) send(1);
            for (int i = 0; i < SKILLS.length; i++) {
                while (SKILLS[i].consumeClick()) send(10 + i);
            }
        });
    }
    private UnityKeys() { }
}
