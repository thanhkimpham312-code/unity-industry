package com.unityindustry.entity;

import com.unityindustry.sound.ReworkSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Scrap Pounce with warning + impact; coordinated pursuit via nearby crawlers. */
public final class ScrapCrawlerEntity extends Spider {
    private int appliedStage = -1, charge = 0, cooldown = 90, dashFrames = 0;
    private boolean overclocked;
    private LivingEntity locked;
    public ScrapCrawlerEntity(EntityType<? extends Spider> type, Level level) { super(type, level); }

    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 10.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server) || !isAlive()) return;
        LivingEntity target = getTarget();
        int stage = ReworkCombat.stage(server);
        boolean pack = target != null && tickCount % 35 < 25 && server.getEntitiesOfClass(
            ScrapCrawlerEntity.class, getBoundingBox().inflate(8.0D),
            other -> other != this && other.isAlive() && other.getTarget() == target).size() >= 2;
        if (stage != appliedStage || pack != overclocked) {
            ReworkCombat.apply(this, stage, 16, 3, 0.30, 2, pack);
            appliedStage = stage;
            overclocked = pack;
        }
        ReworkActions.step(this, "scrap_crawler", 9);
        if (cooldown > 0) cooldown--;
        if (charge > 0) {
            charge--;
            if (charge % 4 == 0) ReworkActions.warning(server, this, 3);
            if (charge == 0 && ReworkActions.canHit(this, locked, 7)) {
                double dx = locked.getX() - getX(), dz = locked.getZ() - getZ();
                double length = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                push(dx / length * 1.05D, 0.28D, dz / length * 1.05D);
                dashFrames = 9;
                ReworkActions.cue(this, "scrap_crawler", "attack", 0.95F);
            }
        } else if (dashFrames > 0) {
            dashFrames--;
            if (ReworkActions.canHit(this, locked, 2.1D)) {
                ReworkActions.strike(server, this, locked, (float)(3.0D + stage), 0.23D);
                ReworkActions.impact(server, this, 9);
                dashFrames = 0;
            }
        } else if (cooldown == 0 && ReworkActions.canHit(this, target, 6.5D) && distanceToSqr(target) > 4.0D) {
            charge = 14; cooldown = 120; locked = target;
            ReworkActions.cue(this, "scrap_crawler", "charge", 0.9F);
        }
    }
    @Override protected SoundEvent getAmbientSound() { return ReworkSounds.cue("scrap_crawler", "ambient"); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ReworkSounds.cue("scrap_crawler", "hurt"); }
    @Override protected SoundEvent getDeathSound() { return ReworkSounds.cue("scrap_crawler", "death"); }
}
