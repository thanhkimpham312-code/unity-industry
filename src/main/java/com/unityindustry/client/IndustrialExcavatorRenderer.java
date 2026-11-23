package com.unityindustry.client;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.resources.Identifier;
/** Reskinned Iron Golem chassis, temporary heavy robot geometry. */
public final class IndustrialExcavatorRenderer extends IronGolemRenderer {
    private static final Identifier SKIN = Identifier.fromNamespaceAndPath("unityindustry", "textures/entity/excavator_mk1.png");
    public IndustrialExcavatorRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public Identifier getTextureLocation(IronGolemRenderState state) { return SKIN; }
}
