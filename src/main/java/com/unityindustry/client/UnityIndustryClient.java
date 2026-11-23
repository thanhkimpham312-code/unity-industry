package com.unityindustry.client;

import com.unityindustry.registry.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import com.unityindustry.registry.ModEntities;
import net.minecraft.resources.Identifier;



public final class UnityIndustryClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        MenuScreens.register(ModMenus.INDUSTRY, StationScreen::new);
        MenuScreens.register(ModMenus.RESEARCH, StationScreen::new);
        MenuScreens.register(ModMenus.IMPLANT, ImplantScreen::new);
        MenuScreens.register(ModMenus.REVERSE, ImplantScreen::new);
        EntityRenderers.register(ModEntities.SCRAP_CRAWLER, ScrapCrawlerRenderer::new);
        EntityRenderers.register(ModEntities.MINING_DRONE, ctx -> new IndustrialBlazeRenderer(ctx,
            Identifier.fromNamespaceAndPath("unityindustry", "textures/entity/mining_drone.png")));
        EntityRenderers.register(ModEntities.CRYSTAL_BEETLE, IndustrialBeetleRenderer::new);
        EntityRenderers.register(ModEntities.SECURITY_SENTINEL, ctx -> new IndustrialBlazeRenderer(ctx,
            Identifier.fromNamespaceAndPath("unityindustry", "textures/entity/security_sentinel.png")));
        EntityRenderers.register(ModEntities.EXCAVATOR_MK1, IndustrialExcavatorRenderer::new);
        UnityKeys.initialize();
    }
}
