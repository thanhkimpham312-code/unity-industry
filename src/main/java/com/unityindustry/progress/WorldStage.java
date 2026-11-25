package com.unityindustry.progress;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unityindustry.UnityIndustry;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Shared multiplayer world stage, permanently upgraded using the highest player advancement.
 * The decision whether to stay upgraded when that player leaves is still provisional.
 */
public final class WorldStage extends SavedData {
    private int stage;

    private static final Codec<WorldStage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("stage").forGetter(s -> s.stage)
    ).apply(instance, WorldStage::new));
    private static final SavedDataType<WorldStage> TYPE = new SavedDataType<>(
        "unityindustry_world_stage", WorldStage::new, CODEC, DataFixTypes.LEVEL);

    private WorldStage() { this(1); }
    private WorldStage(int stage) { this.stage = Math.max(1, Math.min(5, stage)); }

    public static WorldStage get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public static int current(MinecraftServer server) {
        return get(server).stage;
    }

    private void upgradeTo(int reached) {
        if (reached > stage) {
            stage = Math.min(reached, 5);
            setDirty();
            UnityIndustry.LOGGER.info("World industrial threat advanced to stage {}", stage);
        }
    }

    public static void tick(MinecraftServer server) {
        // Poll every 40 server ticks rather than every tick. Players are checked globally.
        if (server.getTickCount() % 40 != 0) return;
        int highest = 1;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            highest = Math.max(highest, highestUnlocked(server, player));
        }
        get(server).upgradeTo(highest);
    }

    private static int highestUnlocked(MinecraftServer server, ServerPlayer player) {
        for (int stage = 5; stage >= 2; stage--) {
            AdvancementHolder advancement = server.getAdvancements().get(UnityIndustry.id("stages/stage_" + stage));
            if (advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone()) {
                return stage;
            }
        }
        return 1;
    }
}
