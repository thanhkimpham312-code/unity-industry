package com.unityindustry.entity;

import com.unityindustry.sound.ReworkSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Ground quadruped turret: no Blaze flight/fireball AI; telegraphed 3-shot volley. */
public final class SecuritySentinelEntity extends Monster {
    private int appliedStage = -1, volleyCharge = 0, volleyCooldown = 45;
    private int volleyShots = 0, shotGap = 0, shockCharge = 0, shockCooldown = 90;
    private LivingEntity locked;
    public SecuritySentinelEntity(EntityType<? extends SecuritySentinelEntity> type, Level level) { super(type, level); }

    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.75D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server) || !isAlive()) return;
        int stage = ReworkCombat.stage(server);
        if (stage != appliedStage) {
            ReworkCombat.apply(this, stage, 30, 5, 0.20, 8, false);
            appliedStage = stage;
        }
        ReworkActions.step(this, "security_sentinel", 15);
        if (volleyCooldown > 0) volleyCooldown--;
        if (shockCooldown > 0) shockCooldown--;
        LivingEntity target = getTarget();
        // Keeps 8-14 blocks when possible, but navigates on the GROUND.
        if (target != null && target.isAlive() && volleyCharge == 0 && volleyShots == 0) {
            if (distanceToSqr(target) > 144.0D) getNavigation().moveTo(target, 0.95D);
            else getNavigation().stop();
        }
        if (shockCharge > 0) {
            shockCharge--;
            if (shockCharge % 4 == 0) ReworkActions.warning(server, this, 4);
            if (shockCharge == 0) {
                ReworkActions.cue(this, "security_sentinel", "special", 1.2F);
                ReworkActions.impact(server, this, 22);
                for (Player p : server.getEntitiesOfClass(Player.class, getBoundingBox().inflate(3D))) {
                    if (ReworkActions.canHit(this, p, 3D)) ReworkActions.strike(server, this, p, 2.0F + stage, 0.65D);
                }
            }
            return;
        }
        if (shockCooldown == 0 && ReworkActions.canHit(this, target, 3.0D)) {
            shockCharge = 20; shockCooldown = 240;
            ReworkActions.cue(this, "security_sentinel", "charge", 1.0F);
            return;
        }
        if (volleyCharge > 0) {
            volleyCharge--;
            if (volleyCharge % 4 == 0) ReworkActions.warning(server, this, 4);
            if (volleyCharge == 0) { volleyShots = 3; shotGap = 0; }
        } else if (volleyShots > 0) {
            if (shotGap > 0) shotGap--;
            else {
                volleyShots--; shotGap = 7;
                ReworkActions.cue(this, "security_sentinel", "attack", 0.95F);
                if (ReworkActions.canHit(this, locked, 18.0D)) {
                    ReworkActions.strike(server, this, locked, 2.0F + stage, 0.08D);
                    ReworkActions.beam(server, this, locked);
                }
            }
        } else if (volleyCooldown == 0 && ReworkActions.canHit(this, target, 18.0D)) {
            volleyCharge = 24; volleyCooldown = 170; locked = target;
            ReworkActions.cue(this, "security_sentinel", "alert", 1.2F);
        }
    }
    @Override protected SoundEvent getAmbientSound() { return ReworkSounds.cue("security_sentinel", "ambient"); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ReworkSounds.cue("security_sentinel", "hurt"); }
    @Override protected SoundEvent getDeathSound() { return ReworkSounds.cue("security_sentinel", "death"); }
}
