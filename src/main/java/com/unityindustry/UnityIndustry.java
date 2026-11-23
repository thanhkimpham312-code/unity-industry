package com.unityindustry;

import com.unityindustry.gameplay.UnityEnergy;
import com.unityindustry.gameplay.UnityPowers;
import com.unityindustry.network.UnityNetworking;
import com.unityindustry.registry.CoreComponents;
import com.unityindustry.registry.ModBlocks;
import com.unityindustry.registry.ModItems;
import com.unityindustry.registry.ModEntities;
import com.unityindustry.registry.ModMenus;
import net.fabricmc.api.ModInitializer;

/** V0.4 prototype: working stations, armor core attachment, charge and selected passive effects. */
public final class UnityIndustry implements ModInitializer {
    public static final String MOD_ID = "unityindustry";
    @Override public void onInitialize() {
        CoreComponents.initialize();
        ModEntities.initialize();
        UnityEnergy.initialize();
        ModBlocks.initialize();
        ModItems.initialize();
        ModMenus.initialize();
        UnityNetworking.initialize();
        UnityPowers.initialize();
    }
}
