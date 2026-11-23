package com.unityindustry.client;
import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
/** Original armor-toned textures on Vanilla Blaze geometry, prototype drone shell. */
public final class IndustrialBlazeRenderer extends BlazeRenderer {
    private final Identifier variant;
    public IndustrialBlazeRenderer(EntityRendererProvider.Context context, Identifier variant) {
        super(context); this.variant = variant;
    }
    @Override public Identifier getTextureLocation(LivingEntityRenderState state) { return variant; }
}
