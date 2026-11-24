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

/** Five original, independently shaped cuboid rigs, NOT vanilla spider/blaze/golem models.
 * All geometry is client-side and bakes into the standard Minecraft entity pipeline.
 */
public final class IndustrialMobModel extends EntityModel<LivingEntityRenderState> {
    public enum Kind { SCRAP_CRAWLER, MINING_DRONE, CRYSTAL_BEETLE, SECURITY_SENTINEL, EXCAVATOR_MK1 }
    private final Kind kind;
    private final ModelPart rootPart;
    private final ModelPart[] animatedLegs;
    private final ModelPart head;

    public IndustrialMobModel(ModelPart root, Kind kind) {
        super(root);
        this.kind = kind;
        this.rootPart = root;
        this.head = root.hasChild("head") ? root.getChild("head") : null;
        if (kind == Kind.SCRAP_CRAWLER) this.animatedLegs = legs(root, 8);
        else if (kind == Kind.CRYSTAL_BEETLE) this.animatedLegs = legs(root, 6);
        else if (kind == Kind.EXCAVATOR_MK1) this.animatedLegs = legs(root, 2);
        else this.animatedLegs = new ModelPart[0];
    }

    private static ModelPart[] legs(ModelPart root, int count) {
        ModelPart[] found = new ModelPart[count];
        for (int i = 0; i < count; i++) found[i] = root.getChild("leg_" + i);
        return found;
    }

    @Override public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float motion = state.walkAnimationPos * 0.66f;
        float amplitude = Math.min(1.0f, state.walkAnimationSpeed) * 0.42f;
        for (int i = 0; i < animatedLegs.length; i++) {
            animatedLegs[i].xRot = Mth.cos(motion + (i % 2 == 0 ? 0.0f : Mth.PI)) * amplitude;
        }
        if (head != null) {
            head.yRot = state.yRot * Mth.DEG_TO_RAD;
            head.xRot = state.xRot * Mth.DEG_TO_RAD;
        }
        if (kind == Kind.MINING_DRONE || kind == Kind.SECURITY_SENTINEL) {
            for (int i = 0; i < 4; i++) {
                ModelPart rotor = rootPart.getChild("rotor_" + i);
                rotor.yRot = state.ageInTicks * (i % 2 == 0 ? 0.6f : -0.6f);
            }
        }
    }

    private static void cube(PartDefinition parent, String name, int u, int v,
                             float x, float y, float z, float w, float h, float d,
                             float px, float py, float pz) {
        parent.addOrReplaceChild(name,
            CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, w, h, d),
            PartPose.offset(px, py, pz));
    }

    public static LayerDefinition layer(Kind kind) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        switch (kind) {
            case SCRAP_CRAWLER -> {
                // Low armored scrap chassis, separate forward sensor head, 8 articulated claws.
                cube(root,"body",0,0, -6,-3,-8, 12,6,16, 0,13,0);
                cube(root,"rear_shell",64,0, -5,-3,-5, 10,5,10, 0,10,5);
                cube(root,"head",64,25, -4,-3,-4, 8,5,7, 0,11,-8);
                cube(root,"eye",96,3, -3,-1,-1, 6,2,2, 0,11,-12);
                for (int i=0;i<8;i++) {
                    int side=i<4?-1:1, j=i%4;
                    cube(root,"leg_"+i,0,52,
                        side<0?-9:0, -1,-1, 9,2,2,
                        side*5.5f, 16, (j-1.5f)*4.0f);
                    cube(root,"claw_"+i,70,55,-1,-1,-1,2,5,2,
                        side*14.0f, 18, (j-1.5f)*4.0f);
                }
            }
            case MINING_DRONE -> {
                // Compact flying industrial scanner with four lift motors and a central lens.
                cube(root,"body",0,0,-6,-4,-6,12,8,12,0,10,0);
                cube(root,"head",64,25,-3,-2,-2,6,4,4,0,10,-6);
                cube(root,"eye",96,3,-3,-2,-1,6,4,2,0,10,-9);
                cube(root,"bottom_thruster",64,46,-3,0,-3,6,3,6,0,15,0);
                for (int i=0;i<4;i++) {
                    int x=(i%2==0?-1:1), z=(i/2==0?-1:1);
                    cube(root,"motor_"+i,0,52,-3,-2,-3,6,4,6, x*9,11,z*9);
                    cube(root,"rotor_"+i,66,82,-5,-1,-1,10,2,2,x*9,8,z*9);
                    cube(root,"strut_"+i,92,46,-1,-1,-4,2,2,8,x*5,12,z*5);
                }
            }
            case CRYSTAL_BEETLE -> {
                // Six-legged beetle with genuinely protruding cyan mineral dorsal crystals.
                cube(root,"body",0,0,-5,-3,-7,10,6,14,0,13,0);
                cube(root,"shell",64,0,-5,-3,-6,10,4,12,0,10,0);
                cube(root,"head",64,25,-3,-3,-4,6,5,7,0,12,-8);
                cube(root,"eye",96,3,-2,-1,-1,4,2,2,0,11,-12);
                for (int i=0;i<6;i++) {
                    int side=i<3?-1:1, j=i%3;
                    cube(root,"leg_"+i,0,52,side<0?-7:0,-1,-1,7,3,2,
                        side*4.7f,19,(j-1)*5.0f);
                }
                for (int i=0;i<5;i++) {
                    cube(root,"crystal_"+i,96,30,-2,-7,-2,4,8,4,
                        (i%2==0?-2.0f:2.0f),10,(i-2)*3.0f);
                }
            }
            case SECURITY_SENTINEL -> {
                // Flying weapon turret: separate dual cannons, body sensor and antennae.
                cube(root,"body",0,0,-6,-5,-5,12,10,10,0,10,0);
                cube(root,"head",64,25,-4,-3,-3,8,6,6,0,10,-5);
                cube(root,"eye",96,3,-3,-2,-1,6,4,2,0,10,-9);
                cube(root,"cannon_left",0,52,-3,-3,-6,6,6,12,-9,11,0);
                cube(root,"cannon_right",0,52,-3,-3,-6,6,6,12,9,11,0);
                cube(root,"bottom_thruster",64,46,-3,0,-3,6,4,6,0,18,1);
                for (int i=0;i<4;i++) {
                    int x=i%2==0?-1:1, z=i/2==0?-1:1;
                    cube(root,"rotor_"+i,66,82,-4,-1,-1,8,2,2,x*8,6,z*7);
                }
                cube(root,"antenna",95,52,-1,-7,-1,2,8,2,0,4,0);
            }
            case EXCAVATOR_MK1 -> {
                // Heavy bipedal industrial boss: massive articulated arms and drill nozzles.
                cube(root,"body",0,0,-8,-8,-5,16,16,10,0,10,0);
                cube(root,"head",64,25,-5,-5,-4,10,8,8,0,-2,-1);
                cube(root,"eye",96,3,-4,-2,-1,8,3,2,0,-3,-5);
                cube(root,"reactor",100,29,-3,-3,-1,6,6,2,0,10,-6);
                cube(root,"left_arm",0,52,-3,-1,-3,6,13,6,-11,8,0);
                cube(root,"right_arm",0,52,-3,-1,-3,6,13,6,11,8,0);
                cube(root,"left_drill",64,76,-3,0,-3,6,8,6,-11,16,-2);
                cube(root,"right_drill",64,76,-3,0,-3,6,8,6,11,16,-2);
                for (int i=0;i<2;i++) {
                    int x=i==0?-1:1;
                    cube(root,"leg_"+i,40,88,-4,-1,-4,8,10,8,x*4,17,0);
                    cube(root,"foot_"+i,0,93,-4,-1,-6,8,4,12,x*4,22,-2);
                }
            }
        }
        return LayerDefinition.create(mesh, 128, 128);
    }
}
