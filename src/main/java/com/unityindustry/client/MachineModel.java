package com.unityindustry.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Early mechanical humanoid geometry. Shared between machine prototypes. */
public final class MachineModel extends EntityModel<MachineRenderState> {
    public MachineModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("torso", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-6, -6, -3, 12, 12, 6), PartPose.offset(0, 13, 0));
        root.addOrReplaceChild("core_head", CubeListBuilder.create()
            .texOffs(0, 18).addBox(-4, -4, -4, 8, 8, 8), PartPose.offset(0, 3, 0));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
            .texOffs(32, 18).addBox(-2, 0, -2, 4, 12, 4), PartPose.offset(8, 8, 0));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
            .texOffs(32, 18).addBox(-2, 0, -2, 4, 12, 4), PartPose.offset(-8, 8, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
            .texOffs(0, 36).addBox(-2, 0, -2, 4, 9, 4), PartPose.offset(3, 16, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
            .texOffs(0, 36).addBox(-2, 0, -2, 4, 9, 4), PartPose.offset(-3, 16, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }
}
