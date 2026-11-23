package com.unityindustry.gameplay;

import com.mojang.serialization.Codec;
import com.unityindustry.UnityIndustry;
import com.unityindustry.registry.ModItems;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Long-based, uncapped energy; persistent and copied on death by Fabric attachments. */
public final class UnityEnergy {
    private static final Map<UUID, Long> LAST_CHARGE = new HashMap<>();
    public static final AttachmentType<Long> ENERGY = AttachmentRegistry.create(
        Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, "unity_energy"),
        b -> b.initializer(() -> 0L).persistent(Codec.LONG).copyOnDeath());
    public static final AttachmentType<Boolean> UNLOCKED = AttachmentRegistry.create(
        Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, "science_unlocked"),
        b -> b.initializer(() -> false).persistent(Codec.BOOL).copyOnDeath());

    public static long get(ServerPlayer player) { return player.getAttachedOrElse(ENERGY, 0L); }
    public static boolean unlocked(ServerPlayer player) { return player.getAttachedOrElse(UNLOCKED, false); }
    public static void unlock(ServerPlayer player) { player.setAttached(UNLOCKED, true); }

    public static boolean consume(ServerPlayer player, long amount) {
        if (amount < 0 || get(player) < amount) return false;
        player.setAttached(ENERGY, get(player) - amount);
        return true;
    }

    /** Server checks inventory itself: the client never chooses the UE amount. */
    public static boolean chargeOne(ServerPlayer player) {
        long now = player.level().getGameTime();
        Long last = LAST_CHARGE.get(player.getUUID());
        if (last != null && now - last < 4L) return false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.UNITY_INGOT)) {
                long current = get(player);
                if (current > Long.MAX_VALUE - 100L) return false;
                stack.shrink(1);
                player.setAttached(ENERGY, current + 100L);
                LAST_CHARGE.put(player.getUUID(), now);
                unlock(player);
                return true;
            }
        }
        return false;
    }
    public static void initialize() { }
    private UnityEnergy() { }
}
