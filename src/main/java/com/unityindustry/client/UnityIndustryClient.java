package com.unityindustry.client;

import com.unityindustry.registry.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import com.unityindustry.registry.ModEntities;

public final class UnityIndustryClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        MenuScreens.register(ModMenus.INDUSTRY, StationScreen::new);
        MenuScreens.register(ModMenus.RESEARCH, StationScreen::new);
        MenuScreens.register(ModMenus.IMPLANT, ImplantScreen::new);
        MenuScreens.register(ModMenus.REVERSE, ImplantScreen::new);
        EntityRenderers.register(ModEntities.SCRAP_CRAWLER, ScrapCrawlerRenderer::new);
        UnityKeys.initialize();
    }
}
