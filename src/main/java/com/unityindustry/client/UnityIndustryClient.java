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

/** Keep original UI, scanner and controls; dedicated 3D rigs for every mob/boss. */
public final class UnityIndustryClient implements ClientModInitializer {
    // Retain the old model layer constant for binary/source compatibility with MachineRenderer.
    public static final ModelLayerLocation MACHINE_LAYER =
        new ModelLayerLocation(UnityIndustry.id("machine"), "main");

    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenus.INDUSTRY, StationScreen::new);
        MenuScreens.register(ModMenus.RESEARCH, StationScreen::new);
        MenuScreens.register(ModMenus.IMPLANT, ImplantScreen::new);
        MenuScreens.register(ModMenus.REVERSE, ImplantScreen::new);

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
            "textures/entity/security_sentinel.png", 1.20f));
        EntityRenderers.register(ModEntities.EXCAVATOR_MK1, ctx -> new ReworkedIndustrialRenderer<>(
            ctx, ReworkedIndustrialLayers.EXCAVATOR, ReworkedIndustrialModel.Kind.EXCAVATOR_MK1,
            "textures/entity/excavator_mk1.png", 1.75f));

        // Keep the original shared-machine layer usable by old renderer classes.
        EntityModelLayerRegistry.registerModelLayer(MACHINE_LAYER, MachineModel::createLayer);
        PrototypeMachineLayers.register();
        registerPrototype("rivet_hound", PrototypeMachineLayers.RIVET_HOUND,
            PrototypeMachineModel.Kind.RIVET_HOUND, 0.68f);
        registerPrototype("bolt_sentry", PrototypeMachineLayers.BOLT_SENTRY,
            PrototypeMachineModel.Kind.BOLT_SENTRY, 0.6f);
        registerPrototype("smelter_brute", PrototypeMachineLayers.SMELTER_BRUTE,
            PrototypeMachineModel.Kind.SMELTER_BRUTE, 0.9f);
        registerPrototype("conveyor_stalker", PrototypeMachineLayers.CONVEYOR_STALKER,
            PrototypeMachineModel.Kind.CONVEYOR_STALKER, 0.75f);
        registerPrototype("arc_warden", PrototypeMachineLayers.ARC_WARDEN,
            PrototypeMachineModel.Kind.ARC_WARDEN, 0.75f);
        registerPrototype("press_titan", PrototypeMachineLayers.PRESS_TITAN,
            PrototypeMachineModel.Kind.PRESS_TITAN, 1.2f);
        registerPrototype("prime_enforcer", PrototypeMachineLayers.PRIME_ENFORCER,
            PrototypeMachineModel.Kind.PRIME_ENFORCER, 1.0f);
        registerPrototype("furnace_tyrant", PrototypeMachineLayers.FURNACE_TYRANT,
            PrototypeMachineModel.Kind.FURNACE_TYRANT, 1.55f);
        registerPrototype("assembly_overlord", PrototypeMachineLayers.ASSEMBLY_OVERLORD,
            PrototypeMachineModel.Kind.ASSEMBLY_OVERLORD, 1.72f);
        registerPrototype("prime_engine", PrototypeMachineLayers.PRIME_ENGINE,
            PrototypeMachineModel.Kind.PRIME_ENGINE, 2.0f);

        OreScannerHighlight.initialize();
        UnityKeys.initialize();
    }

    private static void registerPrototype(String id, ModelLayerLocation layer,
                                          PrototypeMachineModel.Kind kind, float shadow) {
        var type = ModMachines.TYPES.get(id);
        if (type == null) throw new IllegalStateException("Missing industrial machine: " + id);
        EntityRenderers.register(type, ctx -> new PrototypeMachineRenderer<>(ctx,
            layer, kind, "textures/entity/" + id + ".png", shadow));
    }
}
