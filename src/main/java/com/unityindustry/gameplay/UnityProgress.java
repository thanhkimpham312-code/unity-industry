package com.unityindustry.gameplay;

import com.mojang.serialization.Codec;
import com.unityindustry.UnityIndustry;
import com.unityindustry.rules.WorldStageRules;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Achievements and world progression; highest stage ever attained is persistent.
 * All players in the same world use the same stage, including new arrivals.
 * The stage thresholds are provisional until combat balance is approved.
 */
public final class UnityProgress {
    public static final AttachmentType<Integer> WORLD_STAGE = AttachmentRegistry.create(
        Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, "world_stage"),
        b -> b.initializer(() -> 0).persistent(Codec.INT));
    private static long ticks;

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (++ticks % 80 != 0) return;
            int stage = worldStage(server);
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                stage = WorldStageRules.maxEver(stage, stageOf(player, server));
            }
            if (stage > worldStage(server)) {
                server.overworld().setAttached(WORLD_STAGE, stage);
            }
        });
    }
    public static int worldStage(MinecraftServer server) {
        if (server == null) return 0;
        ServerLevel overworld = server.overworld();
        return overworld == null ? 0 : WorldStageRules.clamp(overworld.getAttachedOrElse(WORLD_STAGE, 0));
    }
    public static int stageOf(ServerPlayer player, MinecraftServer server) {
        if (has(player, server, "ultra_implant")) return 4;
        if (has(player, server, "three_cores")) return 3;
        if (has(player, server, "first_implant")) return 2;
        if (has(player, server, "industry_birth")) return 1;
        return 0;
    }
    private static boolean has(ServerPlayer player, MinecraftServer server, String id) {
        AdvancementHolder holder = server.getAdvancements().get(
            Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, id));
        return holder != null && player.getAdvancements().getOrStartProgress(holder).isDone();
    }
    public static void award(ServerPlayer player, String id) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        AdvancementHolder holder = server.getAdvancements().get(
            Identifier.fromNamespaceAndPath(UnityIndustry.MOD_ID, id));
        if (holder != null) player.getAdvancements().award(holder, "done");
    }
    private UnityProgress() { }
}
