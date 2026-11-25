package com.unityindustry.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

public final class PrototypeMachineRenderer<T extends Mob>
        extends MobRenderer<T, LivingEntityRenderState, PrototypeMachineModel> {
    private final Identifier texture;

    public PrototypeMachineRenderer(EntityRendererProvider.Context context,
                                    ModelLayerLocation layer,
                                    PrototypeMachineModel.Kind kind,
                                    String texturePath,
                                    float shadowRadius) {
        super(context, new PrototypeMachineModel(context.bakeLayer(layer), kind), shadowRadius);
        this.texture = Identifier.fromNamespaceAndPath("unityindustry", texturePath);
    }

    @Override public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override public Identifier getTextureLocation(LivingEntityRenderState state) {
        return texture;
    }
}
