package com.unityindustry.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

/** Unique 3D machines using stable existing entity IDs and revised textures. */
public final class ReworkedIndustrialRenderer<T extends Mob>
        extends MobRenderer<T, LivingEntityRenderState, ReworkedIndustrialModel> {
    private final Identifier texture;

    public ReworkedIndustrialRenderer(EntityRendererProvider.Context context,
                                      ModelLayerLocation layer,
                                      ReworkedIndustrialModel.Kind kind,
                                      String texturePath,
                                      float shadowRadius) {
        super(context, new ReworkedIndustrialModel(context.bakeLayer(layer), kind), shadowRadius);
        texture = Identifier.fromNamespaceAndPath("unityindustry", texturePath);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return texture;
    }
}
