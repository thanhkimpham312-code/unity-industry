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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Mine-scanning aerial drone: no vanilla Blaze fireball goal, uses laser wind-up. */
public final class MiningDroneEntity extends Blaze {
    private int appliedStage = -1, charge = 0, cooldown = 35, evadeCooldown = 0;
    private LivingEntity locked;
    public MiningDroneEntity(EntityType<? extends Blaze> type, Level level) { super(type, level); }

    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 15.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server) || !isAlive()) return;
        int stage = ReworkCombat.stage(server);
        if (stage != appliedStage) {
            ReworkCombat.apply(this, stage, 16, 3, 0.25, 2, false);
            appliedStage = stage;
        }
        if (cooldown > 0) cooldown--;
        if (evadeCooldown > 0) evadeCooldown--;
        LivingEntity target = getTarget();
        if (target != null && target.isAlive()) {
            double dx = target.getX() - getX(), dz = target.getZ() - getZ();
            double distance = Math.max(0.5D, Math.sqrt(dx * dx + dz * dz));
            // Constant slow strafing; keep 7-11 blocks instead of hovering like a Blaze turret.
            double direct = distance > 11 ? 0.042D : distance < 7 ? -0.066D : 0.0D;
            double side = Math.sin(tickCount * 0.045D) * 0.028D;
            push(dx / distance * direct - dz / distance * side,
                Math.max(-0.025D, Math.min(0.038D, (target.getY() + 2.6D - getY()) * 0.012D)),
                dz / distance * direct + dx / distance * side);
            if (distance < 4.5D && evadeCooldown == 0) {
                push(-dx / distance * 0.32D, 0.15D, -dz / distance * 0.32D);
                ReworkActions.cue(this, "mining_drone", "special", 0.8F);
                evadeCooldown = 190;
            }
        }
        if (charge > 0) {
            charge--;
            if (charge % 5 == 0) ReworkActions.warning(server, this, 3);
            if (charge == 0 && ReworkActions.canHit(this, locked, 12.0D)) {
                ReworkActions.strike(server, this, locked, (float)(3.5D + stage), 0.18D);
                ReworkActions.cue(this, "mining_drone", "attack", 1.0F);
                ReworkActions.impact(server, this, 12);
            }
        } else if (cooldown == 0 && ReworkActions.canHit(this, target, 12.0D)) {
            charge = 20; cooldown = 140; locked = target;
            ReworkActions.cue(this, "mining_drone", "charge", 1.0F);
        }
    }
    @Override protected SoundEvent getAmbientSound() { return ReworkSounds.cue("mining_drone", "ambient"); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return ReworkSounds.cue("mining_drone", "hurt"); }
    @Override protected SoundEvent getDeathSound() { return ReworkSounds.cue("mining_drone", "death"); }
}
