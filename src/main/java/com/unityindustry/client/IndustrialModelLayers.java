package com.unityindustry.client;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

public final class IndustrialModelLayers {
    public static final ModelLayerLocation SCRAP = main("scrap_crawler");
    public static final ModelLayerLocation DRONE = main("mining_drone");
    public static final ModelLayerLocation BEETLE = main("crystal_beetle");
    public static final ModelLayerLocation SENTINEL = main("security_sentinel");
    public static final ModelLayerLocation EXCAVATOR = main("excavator_mk1");
    private static ModelLayerLocation main(String id) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath("unityindustry", id), "main");
    }
    public static void register() {
        EntityModelLayerRegistry.registerModelLayer(SCRAP,
            () -> IndustrialMobModel.layer(IndustrialMobModel.Kind.SCRAP_CRAWLER));
        EntityModelLayerRegistry.registerModelLayer(DRONE,
            () -> IndustrialMobModel.layer(IndustrialMobModel.Kind.MINING_DRONE));
        EntityModelLayerRegistry.registerModelLayer(BEETLE,
            () -> IndustrialMobModel.layer(IndustrialMobModel.Kind.CRYSTAL_BEETLE));
        EntityModelLayerRegistry.registerModelLayer(SENTINEL,
            () -> IndustrialMobModel.layer(IndustrialMobModel.Kind.SECURITY_SENTINEL));
        EntityModelLayerRegistry.registerModelLayer(EXCAVATOR,
            () -> IndustrialMobModel.layer(IndustrialMobModel.Kind.EXCAVATOR_MK1));
    }
    private IndustrialModelLayers() {}
}
