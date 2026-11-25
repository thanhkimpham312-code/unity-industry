package com.unityindustry.client;

import com.unityindustry.UnityIndustry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;

/** One independent baked model per reworked legacy Mob ID. */
public final class ReworkedIndustrialLayers {
    public static final ModelLayerLocation CRAWLER = layer("rework_scrap_crawler");
    public static final ModelLayerLocation DRONE = layer("rework_mining_drone");
    public static final ModelLayerLocation BEETLE = layer("rework_crystal_beetle");
    public static final ModelLayerLocation SENTINEL = layer("rework_security_sentinel");
    public static final ModelLayerLocation EXCAVATOR = layer("rework_excavator_mk1");

    private ReworkedIndustrialLayers() { }
    private static ModelLayerLocation layer(String path) {
        return new ModelLayerLocation(UnityIndustry.id(path), "main");
    }
    public static void register() {
        EntityModelLayerRegistry.registerModelLayer(CRAWLER,
            () -> ReworkedIndustrialModel.layer(ReworkedIndustrialModel.Kind.SCRAP_CRAWLER));
        EntityModelLayerRegistry.registerModelLayer(DRONE,
            () -> ReworkedIndustrialModel.layer(ReworkedIndustrialModel.Kind.MINING_DRONE));
        EntityModelLayerRegistry.registerModelLayer(BEETLE,
            () -> ReworkedIndustrialModel.layer(ReworkedIndustrialModel.Kind.CRYSTAL_BEETLE));
        EntityModelLayerRegistry.registerModelLayer(SENTINEL,
            () -> ReworkedIndustrialModel.layer(ReworkedIndustrialModel.Kind.SECURITY_SENTINEL));
        EntityModelLayerRegistry.registerModelLayer(EXCAVATOR,
            () -> ReworkedIndustrialModel.layer(ReworkedIndustrialModel.Kind.EXCAVATOR_MK1));
    }
}
