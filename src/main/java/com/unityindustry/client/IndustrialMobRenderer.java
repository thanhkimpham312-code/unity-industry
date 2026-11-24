package com.unityindustry.client;

import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

/** Renderer binds an original industrial 3D mesh and texture to any of the five entities. */
public final class IndustrialMobRenderer<T extends Mob>
        extends MobRenderer<T, LivingEntityRenderState, IndustrialMobModel> {
    private final Identifier texture;

    public IndustrialMobRenderer(EntityRendererProvider.Context context,
                                 ModelLayerLocation layer,
                                 IndustrialMobModel.Kind kind,
                                 String texturePath,
                                 float shadowRadius) {
        super(context, new IndustrialMobModel(context.bakeLayer(layer), kind), shadowRadius);
        this.texture = Identifier.fromNamespaceAndPath("unityindustry", texturePath);
    }

    @Override public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override public Identifier getTextureLocation(LivingEntityRenderState state) {
        return texture;
    }
}
