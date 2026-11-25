package com.unityindustry.client;

import com.unityindustry.UnityIndustry;
import com.unityindustry.entity.ModMachines;
import com.unityindustry.registry.ModEntities;
import com.unityindustry.registry.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;

/** Preserve old gameplay/GUI; render five existing mobs with distinct V2 mechanical models. */
public final class UnityIndustryClient implements ClientModInitializer {
    public static final ModelLayerLocation MACHINE_LAYER =
        new ModelLayerLocation(UnityIndustry.id("machine"), "main");

    @Override
    public void onInitializeClient() {
        // Restore the original four working GUIs.
        MenuScreens.register(ModMenus.INDUSTRY, StationScreen::new);
        MenuScreens.register(ModMenus.RESEARCH, StationScreen::new);
        MenuScreens.register(ModMenus.IMPLANT, ImplantScreen::new);
        MenuScreens.register(ModMenus.REVERSE, ImplantScreen::new);

        // New independent mechanical 3D rigs for the five existing entity IDs.
        ReworkedIndustrialLayers.register();
        EntityRenderers.register(ModEntities.SCRAP_CRAWLER, ctx -> new ReworkedIndustrialRenderer<>(
            ctx, ReworkedIndustrialLayers.CRAWLER, ReworkedIndustrialModel.Kind.SCRAP_CRAWLER,
            "textures/entity/scrap_crawler.png", 0.6f));
        EntityRenderers.register(ModEntities.MINING_DRONE, ctx -> new ReworkedIndustrialRenderer<>(
            ctx, ReworkedIndustrialLayers.DRONE, ReworkedIndustrialModel.Kind.MINING_DRONE,
            "textures/entity/mining_drone.png", 0.35f));
        EntityRenderers.register(ModEntities.CRYSTAL_BEETLE, ctx -> new ReworkedIndustrialRenderer<>(
            ctx, ReworkedIndustrialLayers.BEETLE, ReworkedIndustrialModel.Kind.CRYSTAL_BEETLE,
            "textures/entity/crystal_beetle.png", 0.4f));
        EntityRenderers.register(ModEntities.SECURITY_SENTINEL, ctx -> new ReworkedIndustrialRenderer<>(
            ctx, ReworkedIndustrialLayers.SENTINEL, ReworkedIndustrialModel.Kind.SECURITY_SENTINEL,
            "textures/entity/security_sentinel.png", 0.45f));
        EntityRenderers.register(ModEntities.EXCAVATOR_MK1, ctx -> new ReworkedIndustrialRenderer<>(
            ctx, ReworkedIndustrialLayers.EXCAVATOR, ReworkedIndustrialModel.Kind.EXCAVATOR_MK1,
            "textures/entity/excavator_mk1.png", 0.9f));

        // Restore original client-side gameplay and scanner rendering.
        OreScannerHighlight.initialize();
        UnityKeys.initialize();

        // Add the new V2 Saga machine renderer (10 types: 7 mobs + 3 bosses).
        EntityModelLayerRegistry.registerModelLayer(MACHINE_LAYER, MachineModel::createLayer);
        ModMachines.TYPES.values().forEach(type -> EntityRenderers.register(type, MachineRenderer::new));
    }
}
