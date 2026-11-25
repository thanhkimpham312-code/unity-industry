package com.unityindustry.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.Level;

/** V2 Security Sentinel: hostile autonomous turret and target marking unit. */
public final class SecuritySentinelEntity extends Blaze {
    private int appliedStage = -1;
    private boolean boosted;

    public SecuritySentinelEntity(EntityType<? extends Blaze> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server) || !isAlive()) return;
        int stage = ReworkCombat.stage(server);
        boolean overclock = getTarget() != null && tickCount % 170 < 45;
        if (stage != appliedStage || boosted != overclock) {
            ReworkCombat.apply(this, stage, 26.0D, 5.0D, 0.24D, 5.0D, overclock);
            appliedStage = stage;
            boosted = overclock;
        }
        LivingEntity target = getTarget();
        if (target != null && tickCount % 90 == 0 && distanceToSqr(target) <= 256.0D
            && getSensing().hasLineOfSight(target)) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0), this);
            if (stage >= 2) target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 0), this);
        }
    }
}
