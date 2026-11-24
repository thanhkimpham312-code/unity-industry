package com.unityindustry.client;

import com.unityindustry.registry.ModMenus;
import com.unityindustry.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;

/** All five enemies now render original cuboid rigs, not vanilla mobs. */
public final class UnityIndustryClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        MenuScreens.register(ModMenus.INDUSTRY, StationScreen::new);
        MenuScreens.register(ModMenus.RESEARCH, StationScreen::new);
        MenuScreens.register(ModMenus.IMPLANT, ImplantScreen::new);
        MenuScreens.register(ModMenus.REVERSE, ImplantScreen::new);
        IndustrialModelLayers.register();
        EntityRenderers.register(ModEntities.SCRAP_CRAWLER, ctx -> new IndustrialMobRenderer<>(
            ctx, IndustrialModelLayers.SCRAP, IndustrialMobModel.Kind.SCRAP_CRAWLER,
            "textures/entity/scrap_crawler.png", 0.6f));
        EntityRenderers.register(ModEntities.MINING_DRONE, ctx -> new IndustrialMobRenderer<>(
            ctx, IndustrialModelLayers.DRONE, IndustrialMobModel.Kind.MINING_DRONE,
            "textures/entity/mining_drone.png", 0.35f));
        EntityRenderers.register(ModEntities.CRYSTAL_BEETLE, ctx -> new IndustrialMobRenderer<>(
            ctx, IndustrialModelLayers.BEETLE, IndustrialMobModel.Kind.CRYSTAL_BEETLE,
            "textures/entity/crystal_beetle.png", 0.4f));
        EntityRenderers.register(ModEntities.SECURITY_SENTINEL, ctx -> new IndustrialMobRenderer<>(
            ctx, IndustrialModelLayers.SENTINEL, IndustrialMobModel.Kind.SECURITY_SENTINEL,
            "textures/entity/security_sentinel.png", 0.45f));
        EntityRenderers.register(ModEntities.EXCAVATOR_MK1, ctx -> new IndustrialMobRenderer<>(
            ctx, IndustrialModelLayers.EXCAVATOR, IndustrialMobModel.Kind.EXCAVATOR_MK1,
            "textures/entity/excavator_mk1.png", 0.9f));
        OreScannerHighlight.initialize();
        UnityKeys.initialize();
    }
}
