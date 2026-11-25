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

/** Original screens/renderers/client gameplay plus the new shared mechanical prototype model. */
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

        // Restore original, individual, blocky industrial mob models.
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

        // Restore original client-side gameplay and scanner rendering.
        OreScannerHighlight.initialize();
        UnityKeys.initialize();

        // Add the new V2 Saga machine renderer (10 types: 7 mobs + 3 bosses).
        EntityModelLayerRegistry.registerModelLayer(MACHINE_LAYER, MachineModel::createLayer);
        ModMachines.TYPES.values().forEach(type -> EntityRenderers.register(type, MachineRenderer::new));
    }
}
