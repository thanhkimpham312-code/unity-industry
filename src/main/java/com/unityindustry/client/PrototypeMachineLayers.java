package com.unityindustry.client;

import com.unityindustry.UnityIndustry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;

/** Dedicated layers for the 7 new mobs and 3 bosses. */
public final class PrototypeMachineLayers {
    public static final ModelLayerLocation RIVET_HOUND = layer("rivet_hound");
    public static final ModelLayerLocation BOLT_SENTRY = layer("bolt_sentry");
    public static final ModelLayerLocation SMELTER_BRUTE = layer("smelter_brute");
    public static final ModelLayerLocation CONVEYOR_STALKER = layer("conveyor_stalker");
    public static final ModelLayerLocation ARC_WARDEN = layer("arc_warden");
    public static final ModelLayerLocation PRESS_TITAN = layer("press_titan");
    public static final ModelLayerLocation PRIME_ENFORCER = layer("prime_enforcer");
    public static final ModelLayerLocation FURNACE_TYRANT = layer("furnace_tyrant");
    public static final ModelLayerLocation ASSEMBLY_OVERLORD = layer("assembly_overlord");
    public static final ModelLayerLocation PRIME_ENGINE = layer("prime_engine");

    private static ModelLayerLocation layer(String id) {
        return new ModelLayerLocation(UnityIndustry.id(id), "main");
    }

    public static void register() {
        EntityModelLayerRegistry.registerModelLayer(RIVET_HOUND, () -> PrototypeMachineModel.layer(PrototypeMachineModel.Kind.RIVET_HOUND));
        EntityModelLayerRegistry.registerModelLayer(BOLT_SENTRY, () -> PrototypeMachineModel.layer(PrototypeMachineModel.Kind.BOLT_SENTRY));
        EntityModelLayerRegistry.registerModelLayer(SMELTER_BRUTE, () -> PrototypeMachineModel.layer(PrototypeMachineModel.Kind.SMELTER_BRUTE));
        EntityModelLayerRegistry.registerModelLayer(CONVEYOR_STALKER, () -> PrototypeMachineModel.layer(PrototypeMachineModel.Kind.CONVEYOR_STALKER));
        EntityModelLayerRegistry.registerModelLayer(ARC_WARDEN, () -> PrototypeMachineModel.layer(PrototypeMachineModel.Kind.ARC_WARDEN));
        EntityModelLayerRegistry.registerModelLayer(PRESS_TITAN, () -> PrototypeMachineModel.layer(PrototypeMachineModel.Kind.PRESS_TITAN));
        EntityModelLayerRegistry.registerModelLayer(PRIME_ENFORCER, () -> PrototypeMachineModel.layer(PrototypeMachineModel.Kind.PRIME_ENFORCER));
        EntityModelLayerRegistry.registerModelLayer(FURNACE_TYRANT, () -> PrototypeMachineModel.layer(PrototypeMachineModel.Kind.FURNACE_TYRANT));
        EntityModelLayerRegistry.registerModelLayer(ASSEMBLY_OVERLORD, () -> PrototypeMachineModel.layer(PrototypeMachineModel.Kind.ASSEMBLY_OVERLORD));
        EntityModelLayerRegistry.registerModelLayer(PRIME_ENGINE, () -> PrototypeMachineModel.layer(PrototypeMachineModel.Kind.PRIME_ENGINE));
    }

    private PrototypeMachineLayers() { }
}
