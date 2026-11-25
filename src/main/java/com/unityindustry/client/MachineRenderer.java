package com.unityindustry.client;

import com.unityindustry.UnityIndustry;
import com.unityindustry.entity.MachineEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/** Shared early mechanical model. Individual mob/boss models are not final. */
public final class MachineRenderer extends MobRenderer<MachineEntity, MachineRenderState, MachineModel> {
    public MachineRenderer(EntityRendererProvider.Context context) {
        super(context, new MachineModel(context.bakeLayer(UnityIndustryClient.MACHINE_LAYER)), 0.6f);
    }

    @Override
    public MachineRenderState createRenderState() {
        return new MachineRenderState();
    }

    @Override
    public void extractRenderState(MachineEntity entity, MachineRenderState state, float tickProgress) {
        super.extractRenderState(entity, state, tickProgress);
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        state.texture = UnityIndustry.id("textures/entity/" + key.getPath() + ".png");
    }

    @Override
    public Identifier getTextureLocation(MachineRenderState state) {
        return state.texture;
    }
}
