package com.unityindustry.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

/**
 * Dedicated V2 mechanical models for the five EXISTING entity IDs. These
 * are new meshes, not vanilla Spider/Blaze/IronGolem models or palette swaps.
 * Textures are the new 128x128 industrial atlases bundled in this patch.
 */
public final class ReworkedIndustrialModel extends EntityModel<LivingEntityRenderState> {
    public enum Kind { SCRAP_CRAWLER, MINING_DRONE, CRYSTAL_BEETLE, SECURITY_SENTINEL, EXCAVATOR_MK1 }

    private final Kind kind;
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart[] legs;
    private final ModelPart[] rotors;
    private final ModelPart[] tools;
    private final ModelPart[] wheels;
    private final ModelPart[] turrets;

    public ReworkedIndustrialModel(ModelPart root, Kind kind) {
        super(root);
        this.kind = kind;
        this.root = root;
        this.head = maybe(root, "head");
        this.legs = scan(root, "leg_", kind == Kind.SCRAP_CRAWLER ? 8 :
            kind == Kind.CRYSTAL_BEETLE ? 6 : kind == Kind.SECURITY_SENTINEL ? 4 : 0);
        this.rotors = scan(root, "rotor_", kind == Kind.MINING_DRONE ? 4 : 0);
        this.tools = scan(root, "tool_", kind == Kind.EXCAVATOR_MK1 || kind == Kind.SCRAP_CRAWLER ? 2 :
            kind == Kind.SECURITY_SENTINEL ? 2 : 1);
        this.wheels = scan(root, "wheel_", kind == Kind.EXCAVATOR_MK1 ? 6 : 0);
        this.turrets = scan(root, "turret_", kind == Kind.SECURITY_SENTINEL ? 2 : 0);
    }

    private static ModelPart maybe(ModelPart root, String name) {
        return root.hasChild(name) ? root.getChild(name) : null;
    }

    private static ModelPart[] scan(ModelPart root, String prefix, int length) {
        ModelPart[] result = new ModelPart[length];
        for (int i = 0; i < length; i++) result[i] = maybe(root, prefix + i);
        return result;
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float walk = state.walkAnimationPos * 0.65F;
        float amplitude = Math.min(1F, state.walkAnimationSpeed) * 0.48F;
        for (int i = 0; i < legs.length; i++) {
            if (legs[i] != null) legs[i].xRot = Mth.cos(walk + ((i & 1) == 0 ? 0F : Mth.PI)) * amplitude;
        }
        if (head != null) {
            head.yRot = state.yRot * Mth.DEG_TO_RAD * 0.5F;
            head.xRot = state.xRot * Mth.DEG_TO_RAD * 0.5F;
        }
        for (int i = 0; i < rotors.length; i++) {
            if (rotors[i] != null) rotors[i].yRot = state.ageInTicks * (i % 2 == 0 ? 1.3F : -1.3F);
        }
        for (int i = 0; i < wheels.length; i++) {
            if (wheels[i] != null) wheels[i].xRot = state.walkAnimationPos * 0.6F;
        }
        for (int i = 0; i < turrets.length; i++) {
            if (turrets[i] != null) turrets[i].yRot = Mth.sin(state.ageInTicks * 0.05F + i) * 0.17F;
        }
        for (int i = 0; i < tools.length; i++) {
            if (tools[i] == null) continue;
            if (kind == Kind.MINING_DRONE) tools[i].zRot = state.ageInTicks * 0.85F;
            else if (kind == Kind.SCRAP_CRAWLER) tools[i].yRot = Mth.sin(state.ageInTicks * 0.15F + i) * 0.33F;
            else if (kind == Kind.EXCAVATOR_MK1) tools[i].xRot = Mth.sin(state.ageInTicks * 0.09F + i * Mth.PI) * 0.24F;
            else if (kind == Kind.SECURITY_SENTINEL) tools[i].xRot = Mth.sin(state.ageInTicks * 0.03F + i) * 0.08F;
        }
    }

    private static void cube(PartDefinition root, String name, int u, int v,
                             float x, float y, float z, float w, float h, float d,
                             float pivotX, float pivotY, float pivotZ) {
        root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v)
            .addBox(x, y, z, w, h, d), PartPose.offset(pivotX, pivotY, pivotZ));
    }

    public static LayerDefinition layer(Kind kind) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        switch (kind) {
            case SCRAP_CRAWLER -> {
                cube(root,"body",0,0,-6,-4,-8,12,7,16,0,14,0);
                cube(root,"deck",54,0,-5,-2,-6,10,3,12,0,9,1);
                cube(root,"reactor",100,0,-3,-2,-3,6,3,6,0,8,2);
                cube(root,"head",0,37,-4,-3,-4,8,6,8,0,13,-9);
                cube(root,"optic",45,38,-3,-1,-1,6,2,2,0,12,-13);
                cube(root,"left_exhaust",76,37,-2,-6,-2,4,8,4,-4,8,6);
                cube(root,"right_exhaust",76,37,-2,-6,-2,4,8,4,4,8,6);
                for (int i=0;i<8;i++) {
                    int side=i<4?-1:1; int j=i%4;
                    cube(root,"leg_"+i,0,73,side<0?-9:0,-2,-2,9,4,4,
                        side*6.0F,16,(j-1.5F)*4.2F);
                    cube(root,"piston_"+i,42,74,-2,-2,-2,4,5,4,
                        side*15.0F,18,(j-1.5F)*4.2F);
                }
                cube(root,"tool_0",69,73,-7,-2,-2,10,4,5,-5,16,-12);
                cube(root,"tool_1",69,73,-3,-2,-2,10,4,5,5,16,-12);
            }
            case MINING_DRONE -> {
                cube(root,"body",0,0,-6,-5,-6,12,9,12,0,11,0);
                cube(root,"deck",54,0,-4,-2,-4,8,3,8,0,6,0);
                cube(root,"head",0,37,-4,-3,-5,8,6,7,0,11,-6);
                cube(root,"optic",45,38,-3,-2,-1,6,4,2,0,10,-11);
                cube(root,"tool_0",70,73,-3,-3,-3,6,6,6,0,13,-13);
                cube(root,"drill_barrel",70,89,-2,-2,-5,4,4,8,0,13,-17);
                cube(root,"thruster",97,73,-3,-2,-3,6,6,6,0,18,2);
                for (int i=0;i<4;i++) {
                    int sx=i%2==0?-1:1, sz=i/2==0?-1:1;
                    cube(root,"boom_"+i,0,73,-5,-1,-1,10,2,2,sx*7,11,sz*7);
                    cube(root,"motor_"+i,42,74,-3,-3,-3,6,6,6,sx*11,10,sz*11);
                    cube(root,"rotor_"+i,42,89,-7,-1,-1,14,2,2,sx*11,6,sz*11);
                }
            }
            case CRYSTAL_BEETLE -> {
                cube(root,"body",0,0,-6,-4,-7,12,8,14,0,14,0);
                cube(root,"shell",54,0,-6,-2,-7,12,5,14,0,9,0);
                cube(root,"head",0,37,-4,-3,-5,8,6,8,0,13,-8);
                cube(root,"optic",45,38,-3,-1,-1,6,2,2,0,12,-13);
                cube(root,"reactor",100,0,-3,-3,-3,6,3,6,0,6,-1);
                for (int i=0;i<6;i++) {
                    int side=i<3?-1:1, j=i%3;
                    cube(root,"leg_"+i,0,73,side<0?-8:0,-2,-2,8,4,4,
                        side*6.0F,19,(j-1)*5.2F);
                    cube(root,"piston_"+i,42,74,-2,-1,-2,4,5,4,
                        side*14.0F,21,(j-1)*5.2F);
                }
                for (int i=0;i<5;i++) {
                    cube(root,"crystal_"+i,97,37,-2,-8,-2,4,10,4,
                        (i%2==0?-2.0F:2.0F),8,(i-2)*2.9F);
                }
                cube(root,"tool_0",69,73,-3,-2,-2,6,4,4,0,14,-13);
            }
            case SECURITY_SENTINEL -> {
                // Ground quadruped: low central hull, 4 pistoned legs, rotating twin cannon pods.
                cube(root,"body",0,0,-9,-5,-10,18,10,20,0,13,0);
                cube(root,"deck",54,0,-7,-3,-7,14,4,14,0,8,0);
                cube(root,"reactor",100,0,-4,-3,-2,8,7,4,0,12,-11);
                cube(root,"head",0,37,-5,-4,-4,10,7,8,0,6,-10);
                cube(root,"optic",45,38,-3,-2,-2,6,3,3,0,5,-14);
                cube(root,"antenna",95,73,-1,-8,-1,2,10,2,0,1,6);
                cube(root,"caution_shield",54,0,-5,-2,-2,10,3,4,0,5,4);
                for (int i=0;i<4;i++) {
                    int sx = (i%2 == 0 ? -1 : 1), sz = (i/2 == 0 ? -1 : 1);
                    cube(root,"leg_"+i,0,73,-3,-2,-3,6,8,6,sx*10.5F,13,sz*7.0F);
                    cube(root,"shin_"+i,42,74,-3,-2,-3,6,9,6,sx*13.5F,17,sz*8.0F);
                    cube(root,"foot_"+i,69,73,-4,-2,-6,8,4,12,sx*13.5F,22,sz*8.0F);
                    cube(root,"piston_"+i,100,48,-1,-2,-2,2,10,4,sx*12.0F,14,sz*8.0F);
                }
                for (int i=0;i<2;i++) {
                    int sx = i == 0 ? -1 : 1;
                    cube(root,"turret_"+i,0,52,-4,-4,-6,8,8,12,sx*10.5F,7,-1);
                    cube(root,"tool_"+i,69,73,-2,-2,-7,4,4,14,sx*10.5F,7,-10);
                    cube(root,"barrel_ring_"+i,97,74,-4,-4,-2,8,8,4,sx*10.5F,7,-15);
                }
            }
            case EXCAVATOR_MK1 -> {
                // Low tracked excavator: continuous tread housings instead of golem legs.
                cube(root,"body",0,0,-10,-8,-10,20,16,20,0,12,0);
                cube(root,"deck",54,0,-10,-3,-9,20,5,18,0,5,0);
                cube(root,"cabin",0,37,-7,-6,-7,14,9,14,0,3,-2);
                cube(root,"head",54,37,-5,-4,-3,10,6,7,0,1,-10);
                cube(root,"optic",45,38,-4,-2,-1,8,3,2,0,1,-14);
                cube(root,"reactor",97,37,-4,-4,-2,8,8,4,0,12,-12);
                for (int side=0;side<2;side++) {
                    int x = side == 0 ? -1 : 1;
                    cube(root,"track_"+side,0,73,-5,-7,-13,10,12,26,x*13,19,0);
                    cube(root,"track_guard_"+side,55,70,-6,-3,-14,12,5,28,x*13,14,0);
                    for (int k=0;k<3;k++) {
                        int i=side*3+k;
                        cube(root,"wheel_"+i,42,90,-3,-3,-3,6,6,6,x*19,21,(k-1)*9);
                        cube(root,"tread_"+i,0,102,-5,-2,-4,10,4,8,x*13,23,(k-1)*9);
                    }
                }
                // Two long articulated arms: hydraulic crusher and rotating auger.
                cube(root,"tool_0",0,52,-5,-4,-5,10,12,10,-13,7,-7);
                cube(root,"drill_head",69,73,-4,-4,-11,8,8,14,-13,14,-11);
                cube(root,"drill_tip",97,74,-3,-3,-10,6,6,11,-13,14,-22);
                cube(root,"tool_1",0,52,-5,-4,-5,10,12,10,13,7,-7);
                cube(root,"crusher_upper",69,73,-6,-4,-8,12,5,14,13,16,-10);
                cube(root,"crusher_lower",69,73,-6,1,-8,12,5,14,13,18,-10);
                cube(root,"exhaust_left",93,52,-2,-9,-2,4,13,4,-6,-2,8);
                cube(root,"exhaust_right",93,52,-2,-9,-2,4,13,4,6,-2,8);
                cube(root,"hydraulic_left",42,73,-2,-5,-2,4,9,4,-10,7,3);
                cube(root,"hydraulic_right",42,73,-2,-5,-2,4,9,4,10,7,3);
            }
        }
        return LayerDefinition.create(mesh, 128, 128);
    }
}
