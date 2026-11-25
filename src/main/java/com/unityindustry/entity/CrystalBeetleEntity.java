package com.unityindustry.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** V2 Crystal Beetle: industrial crystal chassis with defensive plating. */
public final class CrystalBeetleEntity extends CaveSpider {
    private int appliedStage = -1;
    private boolean armored;

    public CrystalBeetleEntity(EntityType<? extends CaveSpider> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, true));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 10.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server) || !isAlive()) return;
        int stage = ReworkCombat.stage(server);
        boolean shielded = getHealth() <= getMaxHealth() * 0.5F;
        if (stage != appliedStage || shielded != armored) {
            ReworkCombat.apply(this, stage, 18.0D, 3.0D, 0.27D, shielded ? 10.0D : 3.0D, false);
            appliedStage = stage;
            armored = shielded;
        }
        LivingEntity target = getTarget();
        if (stage >= 1 && target != null && tickCount % 120 == 0
            && distanceToSqr(target) <= 9.0D) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 45, 0), this);
        }
    }
}
