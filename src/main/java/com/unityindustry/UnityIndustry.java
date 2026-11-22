package com.unityindustry;

import net.fabricmc.api.ModInitializer;
import com.unityindustry.registry.ModItems;
import com.unityindustry.registry.ModBlocks;

/** Base registrations only: no fake GUI, energy or mobs in this milestone. */
public final class UnityIndustry implements ModInitializer {
    public static final String MOD_ID = "unityindustry";

    @Override
    public void onInitialize() {
        ModBlocks.initialize();
        ModItems.initialize();
    }
}
