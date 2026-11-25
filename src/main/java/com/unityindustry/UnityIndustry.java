package com.unityindustry;

import com.unityindustry.gameplay.UnityEnergy;
import com.unityindustry.gameplay.UnityHarvest;
import com.unityindustry.gameplay.UnityPowers;
import com.unityindustry.gameplay.UnityProgress;
import com.unityindustry.network.UnityNetworking;
import com.unityindustry.registry.CoreComponents;
import com.unityindustry.registry.ModBlocks;
import com.unityindustry.registry.ModItems;
import com.unityindustry.registry.ModMenus;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Unity Industry: equipment, stations, cores and progression. No custom mobs. */
public final class UnityIndustry implements ModInitializer {
    public static final String MOD_ID = "unityindustry";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        CoreComponents.initialize();
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
        LOGGER.info("Unity Industry industrial systems initialized (no custom entities)");
    }
}
