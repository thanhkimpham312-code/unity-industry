package com.unityindustry.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.unityindustry.network.UnityActionPayload;
import com.unityindustry.gameplay.ChestplateCores;
import com.unityindustry.rules.SkillCatalog;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.network.chat.Component;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/** Configurable keyboard controls; numbering is based on installed manual skills.
 * Slots are dynamically assigned according to worn Cores; passive effects use no slot.
 */
public final class UnityKeys {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath("unityindustry", "science"));
    private static KeyMapping mapping(String id, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.unityindustry." + id, InputConstants.Type.KEYSYM, key, CATEGORY));
    }
    private static final KeyMapping CHARGE = mapping("charge", InputConstants.KEY_X);
    private static final KeyMapping[] SKILLS = new KeyMapping[9];
    private static final boolean[] wasHeld = new boolean[9];
    private static final int[] NUMBERS = {
        InputConstants.KEY_1, InputConstants.KEY_2, InputConstants.KEY_3,
        InputConstants.KEY_4, InputConstants.KEY_5, InputConstants.KEY_6,
        InputConstants.KEY_7, InputConstants.KEY_8, InputConstants.KEY_9
    };
    static {
        for (int i = 0; i < SKILLS.length; i++) SKILLS[i] = mapping("skill_" + (i + 1), NUMBERS[i]);
    }
    private static int heldTicks;
    private static String lastLayout = "";
    private static void send(int action) {
        if (ClientPlayNetworking.canSend(UnityActionPayload.ID))
            ClientPlayNetworking.send(new UnityActionPayload(action));
    }
    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) { heldTicks = 0; lastLayout = ""; return; }
            if (client.screen != null) {
                heldTicks = 0;
                for (int i=0;i<wasHeld.length;i++) { if (wasHeld[i]) send(30+i); wasHeld[i]=false; }
                return;
            }
            if (CHARGE.isDown()) {
                if (heldTicks % 4 == 0) send(0); // 1 ingot per 0.2 second
                heldTicks++;
            } else heldTicks = 0;
            // Only physically registered keys remain stable in Controls. Their ordinal
            // mapping is recomputed from the chestplate on each tick.
            var installed = ChestplateCores.get(client.player.getItemBySlot(EquipmentSlot.CHEST));
            var layout = SkillCatalog.active(installed);
            String layoutText = SkillCatalog.labels(installed);
            if (!layoutText.equals(lastLayout)) {
                for (int i=0;i<wasHeld.length;i++) { if (wasHeld[i]) send(30+i); wasHeld[i]=false; }
                lastLayout = layoutText;
                if (!layout.isEmpty()) client.player.displayClientMessage(
                    Component.literal("§6Unity Skills: " + layoutText), false);
            }
            for (int i = 0; i < SKILLS.length; i++) {
                var action = i < layout.size() ? layout.get(i).action() : null;
                if (action != null && action.hold) {
                    // Holding 1..9 streams start/stop to server; no cooldown is imposed.
                    boolean down = SKILLS[i].isDown();
                    if (down && !wasHeld[i]) send(20 + i);
                    if (!down && wasHeld[i]) send(30 + i);
                    wasHeld[i] = down;
                    // Avoid stale click counts when skill is changed by unimplanting a Core.
                    while (SKILLS[i].consumeClick()) {}
                } else {
                    if (wasHeld[i]) { send(30 + i); wasHeld[i] = false; }
                    while (SKILLS[i].consumeClick()) {
                        if (action != null) send(10 + i);
                    }
                }
            }
        });
    }
    private UnityKeys() { }
}
