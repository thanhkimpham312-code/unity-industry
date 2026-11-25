package com.unityindustry;

import com.unityindustry.entity.ModMachines;
import com.unityindustry.sound.ReworkSounds;
import com.unityindustry.sound.ReworkSounds;
import com.unityindustry.gameplay.UnityEnergy;
import com.unityindustry.gameplay.UnityHarvest;
import com.unityindustry.gameplay.UnityPowers;
import com.unityindustry.gameplay.UnityProgress;
import com.unityindustry.network.UnityNetworking;
import com.unityindustry.registry.CoreComponents;
import com.unityindustry.registry.ModBlocks;
import com.unityindustry.registry.ModEntities;
import com.unityindustry.registry.ModItems;
import com.unityindustry.registry.ModMenus;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration patch: retain all existing industry systems and add new prototype machines.
 * Additive registration. No second mod ID, no second JAR.
 */
public final class UnityIndustry implements ModInitializer {
    public static final String MOD_ID = "unityindustry";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        // The original production/prototype registrations from the working baseline.
        ReworkSounds.initialize();
        ReworkSounds.initialize();
        CoreComponents.initialize();
        ModEntities.initialize();
        UnityEnergy.initialize();
        UnityProgress.initialize();
        ModBlocks.initialize();
        ModItems.initialize();
        ModMenus.initialize();
        UnityNetworking.initialize();
        UnityPowers.initialize();
        UnityHarvest.initialize();
        com.unityindustry.gameplay.UnityAbilitiesPrototype.initialize();
        com.unityindustry.gameplay.UnityRefinery.initialize();

        // New V2 Saga machines are registered ADDITIVELY, without overriding existing IDs.
        ModMachines.register();
        LOGGER.info("Unity Industry: legacy content plus experimental industrial machines initialized");
    }
}
