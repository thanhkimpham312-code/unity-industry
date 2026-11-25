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
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Tracked mining combat machine: shockwave, drill rush, one-time overdrive. */
public final class ExcavatorMk1Entity extends IronGolem {
    private int appliedStage = -1, slamCharge = 0, slamCooldown = 70;
    private int drillCharge = 0, drillCooldown = 125, drillActive = 0;
    private boolean overdrive, overdriveUsed, appliedOverdrive;
    private int overdriveTicks;
    private LivingEntity drillTarget;
    public ExcavatorMk1Entity(EntityType<? extends IronGolem> type, Level level) { super(type, level); }

    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.78D, true));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.65D));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 16.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server) || !isAlive()) return;
        int stage = ReworkCombat.stage(server);
        if (!overdriveUsed && getHealth() < getMaxHealth() * 0.30F) {
            overdriveUsed = true; overdrive = true; overdriveTicks = 120;
            ReworkActions.cue(this, "excavator_mk1", "special", 1.4F);
        }
        if (overdriveTicks > 0 && --overdriveTicks == 0) overdrive = false;
        if (stage != appliedStage || appliedOverdrive != overdrive) {
            ReworkCombat.apply(this, stage, 90, 11, 0.17, 13, overdrive);
            appliedStage = stage;
            appliedOverdrive = overdrive;
        }
        ReworkActions.step(this, "excavator_mk1", 12);
        if (slamCooldown > 0) slamCooldown--;
        if (drillCooldown > 0) drillCooldown--;
        LivingEntity target = getTarget();
        if (slamCharge > 0) {
            slamCharge--;
            if (slamCharge % 5 == 0) ReworkActions.warning(server, this, 8);
            if (slamCharge == 0) {
                ReworkActions.cue(this, "excavator_mk1", "attack", 1.6F);
                ReworkActions.impact(server, this, 45);
                for (Player p : server.getEntitiesOfClass(Player.class, getBoundingBox().inflate(4.0D))) {
                    if (ReworkActions.canHit(this, p, 4.0D))
                        ReworkActions.strike(server, this, p, 4.0F + stage * 1.25F, 0.88D);
                }
            }
        } else if (drillCharge > 0) {
            drillCharge--;
            if (drillCharge % 5 == 0) ReworkActions.warning(server, this, 6);
            if (drillCharge == 0 && ReworkActions.canHit(this, drillTarget, 8.0D)) {
                double dx = drillTarget.getX() - getX(), dz = drillTarget.getZ() - getZ();
                double len = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                push(dx / len * 0.96D, 0.10D, dz / len * 0.96D);
                drillActive = 12;
                ReworkActions.cue(this, "excavator_mk1", "special", 1.25F);
            }
        } else if (drillActive > 0) {
            drillActive--;
            if (ReworkActions.canHit(this, drillTarget, 3.1D)) {
                ReworkActions.strike(server, this, drillTarget, 6.0F + stage, 1.0D);
                drillActive = 0;
                ReworkActions.impact(server, this, 22);
            }
        } else if (slamCooldown == 0 && ReworkActions.canHit(this, target, 4.0D)) {
            slamCharge = 28; slamCooldown = 240;
            ReworkActions.cue(this, "excavator_mk1", "charge", 1.4F);
        } else if (drillCooldown == 0 && ReworkActions.canHit(this, target, 8.0D)) {
            drillCharge = 24; drillCooldown = 280; drillTarget = target;
            ReworkActions.cue(this, "excavator_mk1", "alert", 1.2F);
        }
    }
    @Override protected SoundEvent getAmbientSound() { return ReworkSounds.cue("excavator_mk1", "ambient"); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ReworkSounds.cue("excavator_mk1", "hurt"); }
    @Override protected SoundEvent getDeathSound() { return ReworkSounds.cue("excavator_mk1", "death"); }
}
