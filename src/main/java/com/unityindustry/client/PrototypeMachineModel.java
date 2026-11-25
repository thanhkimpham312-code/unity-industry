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

/** Distinct industrial shapes for every NEW prototype machine and boss. */
public final class PrototypeMachineModel extends EntityModel<LivingEntityRenderState> {
    public enum Kind {
        RIVET_HOUND, BOLT_SENTRY, SMELTER_BRUTE, CONVEYOR_STALKER, ARC_WARDEN,
        PRESS_TITAN, PRIME_ENFORCER, FURNACE_TYRANT, ASSEMBLY_OVERLORD, PRIME_ENGINE
    }
    private final Kind kind;
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart[] legs;
    private final ModelPart[] rotors;
    private final ModelPart[] tools;

    public PrototypeMachineModel(ModelPart root, Kind kind) {
        super(root);
        this.kind = kind;
        this.root = root;
        this.head = root.hasChild("head") ? root.getChild("head") : null;
        this.legs = scan(root, "leg_", switch (kind) {
            case RIVET_HOUND -> 4;
            case CONVEYOR_STALKER -> 6;
            case PRESS_TITAN, PRIME_ENFORCER, FURNACE_TYRANT, ASSEMBLY_OVERLORD, PRIME_ENGINE -> 2;
            default -> 0;
        });
        this.rotors = scan(root, "rotor_", kind == Kind.BOLT_SENTRY || kind == Kind.ARC_WARDEN ? 4 : 0);
        this.tools = scan(root, "tool_", switch (kind) {
            case SMELTER_BRUTE -> 2;
            case PRESS_TITAN, PRIME_ENFORCER, FURNACE_TYRANT, ASSEMBLY_OVERLORD, PRIME_ENGINE -> 2;
            default -> 1;
        });
    }

    private static ModelPart[] scan(ModelPart root, String prefix, int count) {
        ModelPart[] parts = new ModelPart[count];
        for (int i = 0; i < count; i++) parts[i] = root.hasChild(prefix + i) ? root.getChild(prefix + i) : null;
        return parts;
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float walk = state.walkAnimationPos * 0.55F;
        float amp = Math.min(1F, state.walkAnimationSpeed) * 0.45F;
        for (int i = 0; i < legs.length; i++) if (legs[i] != null) legs[i].xRot = Mth.cos(walk + ((i & 1) == 0 ? 0F : Mth.PI)) * amp;
        if (head != null) {
            head.yRot = state.yRot * Mth.DEG_TO_RAD * 0.45F;
            head.xRot = state.xRot * Mth.DEG_TO_RAD * 0.30F;
        }
        for (int i = 0; i < rotors.length; i++) if (rotors[i] != null) rotors[i].yRot = state.ageInTicks * (i % 2 == 0 ? 1.5F : -1.5F);
        for (int i = 0; i < tools.length; i++) if (tools[i] != null) {
            float a = state.ageInTicks * (kind == Kind.PRESS_TITAN ? 0.035F : 0.065F) + i * 0.8F;
            tools[i].xRot = Mth.sin(a) * (kind == Kind.RIVET_HOUND ? 0.4F : 0.21F);
            tools[i].yRot = Mth.cos(a) * 0.10F;
        }
        // Additional mechanical animation: pistons, turret traverse, production cranes.
        ModelPart tailGear = root.hasChild("tail_gear") ? root.getChild("tail_gear") : null;
        if (tailGear != null) tailGear.zRot = state.ageInTicks * 0.10F;
        ModelPart crane = root.hasChild("upper_crane") ? root.getChild("upper_crane") : null;
        if (crane != null) crane.yRot = Mth.sin(state.ageInTicks * 0.035F) * 0.30F;
        ModelPart coil = root.hasChild("charge_module") ? root.getChild("charge_module") : null;
        if (coil != null) coil.yRot = Mth.sin(state.ageInTicks * 0.08F) * 0.09F;
    }

    private static void cube(PartDefinition parent, String name, int u, int v,
                             float x, float y, float z, float w, float h, float d,
                             float px, float py, float pz) {
        parent.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, w, h, d),
            PartPose.offset(px, py, pz));
    }

    public static LayerDefinition layer(Kind kind) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        switch (kind) {
            case RIVET_HOUND -> {
                cube(root,"body",0,0,-6,-4,-10,12,8,20,0,14,0);
                cube(root,"back",48,0,-5,-3,-7,10,4,14,0,9,1);
                cube(root,"head",0,32,-4,-4,-6,8,7,8,0,13,-11);
                cube(root,"jaw",48,22,-4,-1,-7,8,2,8,0,15,-15);
                for (int i=0;i<4;i++) { int sx=i%2==0?-1:1, sz=i/2==0?-1:1;
                    cube(root,"leg_"+i,0,64,-3,-2,-3,6,10,6,sx*6.5F,16,sz*6.5F);
                    cube(root,"hoof_"+i,24,64,-3,-1,-5,6,3,10,sx*8.5F,23,sz*6.5F);
                }
                cube(root,"tool_0",64,64,-2,-2,-8,4,4,12,0,15,-15);
            
                cube(root,"spine_motor",112,0,-4,-4,-5,8,5,10,0,8,3);
                cube(root,"tail_gear",112,18,-3,-3,-3,6,6,6,0,13,11);
                cube(root,"jaw_blade_l",140,18,-1,-2,-8,2,3,10,-3,14,-14);
                cube(root,"jaw_blade_r",140,18,-1,-2,-8,2,3,10,3,14,-14);
}
            case BOLT_SENTRY -> {
                cube(root,"body",0,0,-7,-5,-7,14,10,14,0,10,0);
                cube(root,"head",0,32,-4,-4,-4,8,8,8,0,10,-8);
                cube(root,"optic",32,32,-3,-2,-1,6,4,2,0,10,-13);
                cube(root,"tool_0",48,22,-3,-3,-8,6,6,10,0,11,-15);
                for (int i=0;i<4;i++) { int sx=i%2==0?-1:1, sz=i/2==0?-1:1;
                    cube(root,"boom_"+i,0,64,-4,-1,-1,8,2,2,sx*7,10,sz*7);
                    cube(root,"rotor_"+i,18,64,-6,-1,-1,12,2,2,sx*10,7,sz*10);
                }
            
                cube(root,"sensor_ring",112,0,-5,-5,-2,10,10,4,0,10,-13);
                cube(root,"left_railgun",140,0,-2,-2,-8,4,4,12,-10,11,-7);
                cube(root,"right_railgun",140,0,-2,-2,-8,4,4,12,10,11,-7);
                cube(root,"antenna",160,20,-1,-10,-1,2,12,2,0,6,3);
}
            case SMELTER_BRUTE -> {
                cube(root,"body",0,0,-8,-9,-5,16,18,10,0,11,0);
                cube(root,"head",0,32,-5,-5,-4,10,8,8,0,-2,-1);
                cube(root,"furnace",36,0,-4,-5,-2,8,10,4,0,12,-6);
                cube(root,"tool_0",64,0,-3,-1,-3,6,14,6,-11,8,0);
                cube(root,"tool_1",64,0,-3,-1,-3,6,14,6,11,8,0);
                cube(root,"leg_0",0,64,-4,-1,-4,8,12,8,-4,17,0);
                cube(root,"leg_1",0,64,-4,-1,-4,8,12,8,4,17,0);
            
                cube(root,"shoulder_plate_l",112,0,-4,-3,-5,8,7,10,-11,5,0);
                cube(root,"shoulder_plate_r",112,0,-4,-3,-5,8,7,10,11,5,0);
                cube(root,"exhaust_l",135,0,-2,-12,-2,4,16,4,-4,-1,7);
                cube(root,"exhaust_r",135,0,-2,-12,-2,4,16,4,4,-1,7);
                cube(root,"hydraulic_ram",160,0,-3,-6,-3,6,10,6,0,11,-7);
}
            case CONVEYOR_STALKER -> {
                cube(root,"body",0,0,-7,-4,-12,14,8,24,0,13,0);
                cube(root,"head",0,32,-4,-3,-4,8,6,8,0,12,-13);
                cube(root,"belt",48,0,-6,-1,-8,12,2,16,0,10,0);
                for (int i=0;i<6;i++) { int side=i<3?-1:1, j=i%3;
                    cube(root,"leg_"+i,0,64,side<0?-8:0,-2,-2,8,7,4,side*6.0F,17,(j-1)*6.0F);
                }
                cube(root,"tool_0",48,22,-2,-2,-8,4,4,10,0,14,-17);
            
                cube(root,"track_l",112,0,-4,-5,-12,8,10,24,-9,15,0);
                cube(root,"track_r",112,0,-4,-5,-12,8,10,24,9,15,0);
                cube(root,"front_claw_l",160,0,-3,-2,-8,6,4,12,-8,16,-15);
                cube(root,"front_claw_r",160,0,-3,-2,-8,6,4,12,8,16,-15);
                cube(root,"rear_fin",193,0,-1,-7,-5,2,10,10,0,9,8);
}
            case ARC_WARDEN -> {
                cube(root,"body",0,0,-7,-6,-7,14,12,14,0,12,0);
                cube(root,"head",0,32,-5,-5,-5,10,8,10,0,8,-7);
                cube(root,"coil",48,0,-4,-4,-4,8,8,8,0,12,-11);
                cube(root,"tool_0",48,22,-4,-2,-8,8,4,11,0,14,-14);
                for (int i=0;i<4;i++) { int sx=i%2==0?-1:1, sz=i/2==0?-1:1;
                    cube(root,"rotor_"+i,0,64,-5,-1,-1,10,2,2,sx*9,6,sz*9);
                }
            
                cube(root,"coil_l",112,0,-3,-6,-3,6,12,6,-10,12,0);
                cube(root,"coil_r",112,0,-3,-6,-3,6,12,6,10,12,0);
                cube(root,"arc_rod_l",160,0,-1,-10,-1,2,16,2,-9,7,-5);
                cube(root,"arc_rod_r",160,0,-1,-10,-1,2,16,2,9,7,-5);
                cube(root,"charge_module",193,0,-6,-3,-4,12,6,8,0,7,7);
}
            case PRESS_TITAN -> {
                cube(root,"body",0,0,-10,-12,-7,20,24,14,0,10,0);
                cube(root,"head",0,38,-6,-6,-5,12,9,10,0,-6,-1);
                cube(root,"ram",56,0,-4,-5,-3,8,10,6,0,12,-10);
                cube(root,"tool_0",0,68,-4,-2,-4,8,18,8,-14,8,0);
                cube(root,"tool_1",0,68,-4,-2,-4,8,18,8,14,8,0);
                cube(root,"leg_0",64,68,-5,-1,-5,10,13,10,-5,22,0);
                cube(root,"leg_1",64,68,-5,-1,-5,10,13,10,5,22,0);
            
                cube(root,"press_plate_l",112,0,-7,-3,-6,14,6,12,-14,20,-3);
                cube(root,"press_plate_r",112,0,-7,-3,-6,14,6,12,14,20,-3);
                cube(root,"hydraulic_l",160,0,-2,-12,-2,4,16,4,-13,3,2);
                cube(root,"hydraulic_r",160,0,-2,-12,-2,4,16,4,13,3,2);
                cube(root,"back_ram",193,0,-6,-8,-4,12,15,8,0,5,8);
}
            case PRIME_ENFORCER -> {
                cube(root,"body",0,0,-8,-10,-6,16,20,12,0,11,0);
                cube(root,"head",0,32,-5,-5,-4,10,8,8,0,-3,-1);
                cube(root,"core",48,0,-4,-4,-2,8,8,4,0,11,-7);
                cube(root,"tool_0",64,0,-3,-1,-3,6,15,6,-11,8,0);
                cube(root,"tool_1",64,0,-3,-1,-3,6,15,6,11,8,0);
                cube(root,"leg_0",0,64,-4,-1,-4,8,12,8,-4,19,0);
                cube(root,"leg_1",0,64,-4,-1,-4,8,12,8,4,19,0);
            
                cube(root,"thruster_l",112,0,-4,-6,-5,8,12,10,-8,9,7);
                cube(root,"thruster_r",112,0,-4,-6,-5,8,12,10,8,9,7);
                cube(root,"left_guard",160,0,-5,-4,-7,10,12,8,-10,8,-2);
                cube(root,"right_guard",160,0,-5,-4,-7,10,12,8,10,8,-2);
                cube(root,"sensor_array",193,0,-7,-3,-2,14,5,4,0,-1,-5);
}
            case FURNACE_TYRANT -> {
                cube(root,"body",0,0,-13,-16,-9,26,32,18,0,9,0);
                cube(root,"head",0,50,-7,-7,-6,14,10,12,0,-11,-1);
                cube(root,"furnace",64,0,-6,-7,-3,12,14,6,0,10,-10);
                cube(root,"tool_0",0,80,-5,-2,-5,10,24,10,-18,6,0);
                cube(root,"tool_1",0,80,-5,-2,-5,10,24,10,18,6,0);
                cube(root,"leg_0",80,80,-6,-1,-6,12,16,12,-7,24,0);
                cube(root,"leg_1",80,80,-6,-1,-6,12,16,12,7,24,0);
            
                cube(root,"chimney_l",112,0,-4,-18,-4,8,22,8,-8,-3,8);
                cube(root,"chimney_r",112,0,-4,-18,-4,8,22,8,8,-3,8);
                cube(root,"furnace_guard",160,0,-9,-8,-4,18,14,8,0,11,-12);
                cube(root,"heat_coil",193,0,-7,-4,-6,14,9,12,0,7,11);
}
            case ASSEMBLY_OVERLORD -> {
                cube(root,"body",0,0,-14,-17,-10,28,34,20,0,9,0);
                cube(root,"head",0,54,-7,-7,-5,14,10,10,0,-10,-2);
                cube(root,"forge",72,0,-6,-6,-4,12,12,8,0,9,-12);
                cube(root,"tool_0",0,84,-4,-2,-4,8,22,8,-20,5,-2);
                cube(root,"tool_1",0,84,-4,-2,-4,8,22,8,20,5,-2);
                cube(root,"leg_0",88,84,-6,-1,-6,12,16,12,-8,25,0);
                cube(root,"leg_1",88,84,-6,-1,-6,12,16,12,8,25,0);
            
                cube(root,"assembly_crane_l",112,0,-4,-4,-20,8,8,26,-16,-2,-3);
                cube(root,"assembly_crane_r",112,0,-4,-4,-20,8,8,26,16,-2,-3);
                cube(root,"forge_conveyor",160,0,-10,-5,-8,20,8,16,0,11,10);
                cube(root,"upper_crane",193,0,-4,-12,-10,8,15,16,0,-13,1);
                cube(root,"warning_tower",112,48,-3,-18,-3,6,18,6,0,-14,12);
}
            case PRIME_ENGINE -> {
                cube(root,"body",0,0,-16,-20,-12,32,40,24,0,8,0);
                cube(root,"head",0,64,-8,-8,-6,16,12,12,0,-14,-2);
                cube(root,"reactor",80,0,-7,-8,-4,14,16,8,0,10,-15);
                cube(root,"tool_0",0,96,-5,-2,-5,10,26,10,-22,6,0);
                cube(root,"tool_1",0,96,-5,-2,-5,10,26,10,22,6,0);
                cube(root,"leg_0",96,96,-7,-1,-7,14,18,14,-9,28,0);
                cube(root,"leg_1",96,96,-7,-1,-7,14,18,14,9,28,0);
            
                cube(root,"prime_spine",112,0,-9,-15,-8,18,26,16,0,4,15);
                cube(root,"reactor_ring_top",160,0,-14,-3,-8,28,6,16,0,-5,-10);
                cube(root,"reactor_ring_bottom",160,24,-14,-3,-8,28,6,16,0,20,-10);
                cube(root,"left_antenna",193,0,-3,-22,-3,6,24,6,-15,-4,5);
                cube(root,"right_antenna",193,0,-3,-22,-3,6,24,6,15,-4,5);
}
        }
        return LayerDefinition.create(mesh, 256, 256);
    }
}
