package com.unityindustry.client;

import com.unityindustry.entity.ScrapCrawlerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/** Vanilla spider geometry with an original industrial-steel texture. */
public final class ScrapCrawlerRenderer extends SpiderRenderer<ScrapCrawlerEntity> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
        "unityindustry", "textures/entity/scrap_crawler.png");

    public ScrapCrawlerRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
