package com.unityindustry.client;
import net.minecraft.client.renderer.entity.CaveSpiderRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
/** Recolored CaveSpider base. Dedicated beetle rig/animations are not yet done. */
public final class IndustrialBeetleRenderer extends CaveSpiderRenderer {
    private static final Identifier SKIN = Identifier.fromNamespaceAndPath("unityindustry", "textures/entity/crystal_beetle.png");
    public IndustrialBeetleRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public Identifier getTextureLocation(LivingEntityRenderState state) { return SKIN; }
}
