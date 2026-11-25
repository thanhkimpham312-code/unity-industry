package com.unityindustry.client;

import com.unityindustry.UnityIndustry;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class MachineRenderState extends LivingEntityRenderState {
    public Identifier texture = UnityIndustry.id("textures/entity/scrap_crawler.png");
}
