package com.unityindustry.client;

import com.unityindustry.entity.ModMachines;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderers;
import com.unityindustry.UnityIndustry;

public final class UnityIndustryClient implements ClientModInitializer {
    public static final ModelLayerLocation MACHINE_LAYER = new ModelLayerLocation(UnityIndustry.id("machine"), "main");

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(MACHINE_LAYER, MachineModel::createLayer);
        ModMachines.TYPES.values().forEach(type -> EntityRenderers.register(type, MachineRenderer::new));
    }
}
