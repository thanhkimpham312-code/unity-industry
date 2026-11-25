package com.unityindustry;

import com.unityindustry.entity.ModMachines;
import com.unityindustry.progress.WorldStage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UnityIndustry implements ModInitializer {
    public static final String MOD_ID = "unityindustry";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModMachines.register();
        // The permanent global stage is driven by the highest player's mod advancements.
        // Specific unlock criteria are manual placeholders until gameplay milestones are approved.
        ServerTickEvents.END_SERVER_TICK.register(WorldStage::tick);
        LOGGER.info("Unity Industry V2 Saga: experimental machines registered");
    }
}
