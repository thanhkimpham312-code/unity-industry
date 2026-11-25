package com.unityindustry.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.Level;

/** V2 Mining Drone: hostile aerial rig, with a mining-disruption pulse. */
public final class MiningDroneEntity extends Blaze {
    private int appliedStage = -1;

    public MiningDroneEntity(EntityType<? extends Blaze> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server) || !isAlive()) return;
        int stage = ReworkCombat.stage(server);
        if (stage != appliedStage) {
            ReworkCombat.apply(this, stage, 16.0D, 3.0D, 0.28D, 2.0D, false);
            appliedStage = stage;
        }
        LivingEntity target = getTarget();
        if (target != null && tickCount % 100 == 0 && distanceToSqr(target) <= 144.0D
            && getSensing().hasLineOfSight(target)) {
            target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 75, stage >= 3 ? 1 : 0), this);
        }
    }
}
